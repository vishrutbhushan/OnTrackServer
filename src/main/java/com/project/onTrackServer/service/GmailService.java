package com.project.onTrackServer.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.project.onTrackServer.model.Item;
import com.project.onTrackServer.model.User;
import com.project.onTrackServer.repository.ItemRepository;
import com.project.onTrackServer.exception.GmailAuthenticationException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class GmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(GmailService.class);
    private static final String APPLICATION_NAME = "OnTrack Server";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    
    public List<Item> fetchEmailsFromGmail(User user) {
        logger.info("Fetching emails from Gmail for user: {}", user.getUserId());
        
        try {
            Gmail service = getGmailService(user);
            ListMessagesResponse listResponse = service.users().messages()
                .list("me")
                .setMaxResults(10L)
                .execute();
                
            List<Message> messages = listResponse.getMessages();
            if (messages == null || messages.isEmpty()) {
                logger.info("No messages found for user: {}", user.getUserId());
                return new ArrayList<>();
            }
            
            logger.info("Found {} messages for user: {}", messages.size(), user.getUserId());
            
            return messages.stream()
                .map(message -> getEmailItem(service, message, user.getUserId().toString()))
                .filter(item -> item != null)
                .toList();
                
        } catch (IOException e) {
            logger.error("Error fetching emails from Gmail for user {}: {}", user.getUserId(), e.getMessage());
            
            // Check if it's an authentication error
            if (e.getMessage().contains("401") || e.getMessage().contains("Invalid Credentials") || 
                e.getMessage().contains("UNAUTHENTICATED") || e.getMessage().contains("authError")) {
                throw new GmailAuthenticationException("Gmail authentication failed: " + e.getMessage(), e);
            }
            
            throw new RuntimeException("Failed to fetch emails from Gmail: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error for user {}: {}", user.getUserId(), e.getMessage());
            throw new RuntimeException("Failed to fetch emails: " + e.getMessage());
        }
    }
    
    public List<Item> fetchNewEmailsFromGmail(User user, ItemRepository itemRepository) {
        logger.info("Fetching new emails from Gmail for user: {}", user.getUserId());
        
        try {
            Gmail service = getGmailService(user);
            
            // Fetch recent emails (last 50)
            ListMessagesResponse listResponse = service.users().messages()
                .list("me")
                .setMaxResults(50L)
                .setQ("is:unread") // Only fetch unread emails to get new ones
                .execute();
                
            List<Message> messages = listResponse.getMessages();
            if (messages == null || messages.isEmpty()) {
                logger.info("No new messages found for user: {}", user.getUserId());
                return new ArrayList<>();
            }
            
            logger.info("Found {} potential new messages for user: {}", messages.size(), user.getUserId());
            
            List<Item> newEmails = new ArrayList<>();
            
            for (Message message : messages) {
                Item emailItem = getEmailItem(service, message, user.getUserId().toString());
                if (emailItem != null) {
                    // Check if this email already exists in the database
                    List<Item> existingItems = itemRepository.findByUserIdAndSubjectAndSender(
                        user.getUserId().toString(), emailItem.getSubject(), emailItem.getSender());
                    
                    // If no existing email with same subject and sender, consider it new
                    if (existingItems.isEmpty()) {
                        newEmails.add(emailItem);
                    }
                }
            }
            
            logger.info("Found {} truly new emails for user: {}", newEmails.size(), user.getUserId());
            return newEmails;
                
        } catch (IOException e) {
            logger.error("Error fetching new emails from Gmail for user {}: {}", user.getUserId(), e.getMessage());
            
            // Check if it's an authentication error
            if (e.getMessage().contains("401") || e.getMessage().contains("Invalid Credentials") || 
                e.getMessage().contains("UNAUTHENTICATED") || e.getMessage().contains("authError")) {
                throw new GmailAuthenticationException("Gmail authentication failed: " + e.getMessage(), e);
            }
            
            throw new RuntimeException("Failed to fetch new emails from Gmail: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error fetching new emails for user {}: {}", user.getUserId(), e.getMessage());
            throw new RuntimeException("Failed to fetch new emails: " + e.getMessage());
        }
    }
    
    private Item getEmailItem(Gmail service, Message message, String userId) {
        try {
            Message fullMessage = service.users().messages()
                .get("me", message.getId())
                .setFormat("metadata")
                .execute();
                
            String subject = "No Subject";
            String from = "Unknown Sender";
            
            if (fullMessage.getPayload() != null && fullMessage.getPayload().getHeaders() != null) {
                for (var header : fullMessage.getPayload().getHeaders()) {
                    if ("Subject".equals(header.getName())) {
                        subject = header.getValue();
                    } else if ("From".equals(header.getName())) {
                        from = extractEmail(header.getValue());
                    }
                }
            }
            
            return new Item(subject, "Email content preview...", from, userId);
            
        } catch (Exception e) {
            logger.error("Error processing message {}: {}", message.getId(), e.getMessage());
            return null;
        }
    }
    
    private String extractEmail(String fromHeader) {
        if (fromHeader.contains("<") && fromHeader.contains(">")) {
            return fromHeader.substring(fromHeader.indexOf("<") + 1, fromHeader.indexOf(">"));
        }
        return fromHeader;
    }
    
    private Gmail getGmailService(User user) {
        try {
            if (user.getAuthToken() == null || user.getAuthToken().equals("gmail_access_granted")) {
                throw new IllegalStateException("No valid access token available for user: " + user.getUserId());
            }
            
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            
            Date expiryTime = new Date(System.currentTimeMillis() + (24 * 60 * 60 * 1000));
            AccessToken accessToken = new AccessToken(user.getAuthToken(), expiryTime);
            
            GoogleCredentials credentials = new GoogleCredentials(accessToken) {
                @Override
                public AccessToken refreshAccessToken() throws IOException {
                    logger.debug("Access token refresh requested for user: {}, using existing token", user.getUserId());
                    return getAccessToken();
                }
            };
            
            return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
                
        } catch (GeneralSecurityException | IOException e) {
            logger.error("Error creating Gmail service: {}", e.getMessage());
            throw new RuntimeException("Failed to create Gmail service: " + e.getMessage());
        }
    }
}
