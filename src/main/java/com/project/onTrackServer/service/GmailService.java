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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class GmailService {
    
    private static final String APPLICATION_NAME = "OnTrack Server";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    
    public List<Item> fetchEmailsFromGmail(User user) {
        log.info("Fetching emails from Gmail for user: {}", user.getUserId());
        
        try {
            Gmail service = getGmailService(user);
            ListMessagesResponse listResponse = service.users().messages()
                .list("me")
                .setMaxResults(10L)
                .execute();
                
            List<Message> messages = listResponse.getMessages();
            if (messages == null || messages.isEmpty()) {
                log.info("No messages found for user: {}", user.getUserId());
                return new ArrayList<>();
            }
            
            log.info("Found {} messages for user: {}", messages.size(), user.getUserId());
            
            return messages.stream()
                .map(message -> getEmailItem(service, message, user.getUserId()))
                .filter(item -> item != null)
                .toList();
                
        } catch (IOException e) {
            log.error("Error fetching emails from Gmail for user {}: {}", user.getUserId(), e.getMessage());
            
            // Check if it's an authentication error
            if (e.getMessage().contains("401") || e.getMessage().contains("Invalid Credentials") || 
                e.getMessage().contains("UNAUTHENTICATED") || e.getMessage().contains("authError")) {
                throw new GmailAuthenticationException("Gmail authentication failed: " + e.getMessage(), e);
            }
            
            throw new RuntimeException("Failed to fetch emails from Gmail: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error for user {}: {}", user.getUserId(), e.getMessage());
            throw new RuntimeException("Failed to fetch emails: " + e.getMessage());
        }
    }
    
    public List<Item> fetchNewEmailsFromGmail(User user, ItemRepository itemRepository) {
        log.info("Fetching new emails from Gmail for user: {}", user.getUserId());
        
        try {
            Gmail service = getGmailService(user);
            
            String query = "is:unread";
            
            // If we have a last processed email, fetch only newer ones
            if (user.getUserConfig() != null && user.getUserConfig().getLastProcessedEmailId() != null) {
                query += " after:" + user.getUserConfig().getLastProcessedEmailTime();
            }
            
            // Fetch recent emails
            ListMessagesResponse listResponse = service.users().messages()
                .list("me")
                .setMaxResults(10L) // Reduced from 50 to only get new emails since last processed
                .setQ(query)
                .execute();
                
            List<Message> messages = listResponse.getMessages();
            if (messages == null || messages.isEmpty()) {
                log.info("No new messages found for user: {}", user.getUserId());
                return new ArrayList<>();
            }
            
            log.info("Found {} new messages for user: {}", messages.size(), user.getUserId());
            
            List<Item> newEmails = new ArrayList<>();
            
            for (Message message : messages) {
                Item emailItem = getEmailItem(service, message, user.getUserId());
                if (emailItem != null) {
                    // Check if this email already exists in the database by Gmail message ID
                    if (!itemRepository.existsByGmailMessageId(emailItem.getGmailMessageId())) {
                        newEmails.add(emailItem);
                    }
                }
            }
            
            log.info("Found {} truly new emails for user: {}", newEmails.size(), user.getUserId());
            return newEmails;
                
        } catch (IOException e) {
            log.error("Error fetching new emails from Gmail for user {}: {}", user.getUserId(), e.getMessage());
            
            // Check if it's an authentication error
            if (e.getMessage().contains("401") || e.getMessage().contains("Invalid Credentials") || 
                e.getMessage().contains("UNAUTHENTICATED") || e.getMessage().contains("authError")) {
                throw new GmailAuthenticationException("Gmail authentication failed: " + e.getMessage(), e);
            }
            
            throw new RuntimeException("Failed to fetch new emails from Gmail: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error fetching new emails for user {}: {}", user.getUserId(), e.getMessage());
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
            
            Item item = new Item();
            item.setGmailMessageId(message.getId());
            item.setSubject(subject);
            item.setSender(from);
            item.setSnippet(fullMessage.getSnippet() != null ? fullMessage.getSnippet() : "");
            return item;
            
        } catch (Exception e) {
            log.error("Error processing message {}: {}", message.getId(), e.getMessage());
            return null;
        }
    }
    
    private String extractEmail(String fromHeader) {
        if (fromHeader.contains("<") && fromHeader.contains(">")) {
            return fromHeader.substring(fromHeader.indexOf("<") + 1, fromHeader.indexOf(">"));
        }
        return fromHeader;
    }
    
    public void archiveEmail(User user, String messageId) {
        try {
            Gmail service = getGmailService(user);
            
            // Remove INBOX label to archive the email
            com.google.api.services.gmail.model.Message message = new com.google.api.services.gmail.model.Message();
            message.setId(messageId);
            
            service.users().messages()
                .modify("me", messageId, 
                    new com.google.api.services.gmail.model.ModifyMessageRequest()
                        .setRemoveLabelIds(List.of("INBOX")))
                .execute();
            
            log.info("Archived email with message ID: {} for user: {}", messageId, user.getUserId());
            
        } catch (Exception e) {
            log.warn("Failed to archive email {}: {}", messageId, e.getMessage());
        }
    }
    
    private Gmail getGmailService(User user) {
        try {
            if (user.getAccessToken() == null || user.getAccessToken().equals("gmail_access_granted")) {
                throw new IllegalStateException("No valid access token available for user: " + user.getUserId());
            }
            
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            
            Date expiryTime = new Date(System.currentTimeMillis() + (24 * 60 * 60 * 1000));
            AccessToken accessToken = new AccessToken(user.getAccessToken(), expiryTime);
            
            GoogleCredentials credentials = new GoogleCredentials(accessToken) {
                @Override
                public AccessToken refreshAccessToken() throws IOException {
                    log.debug("Access token refresh requested for user: {}, using existing token", user.getUserId());
                    return getAccessToken();
                }
            };
            
            return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
                
        } catch (GeneralSecurityException | IOException e) {
            log.error("Error creating Gmail service: {}", e.getMessage());
            throw new RuntimeException("Failed to create Gmail service: " + e.getMessage());
        }
    }
}
