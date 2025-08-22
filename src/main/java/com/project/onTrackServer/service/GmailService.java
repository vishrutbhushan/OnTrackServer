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
import com.project.onTrackServer.model.Token;
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
    
    public List<Item> fetchEmailsFromGmail(Token token) {
        logger.info("Fetching emails from Gmail for user: {}", token.getUserId());
        
        try {
            Gmail service = getGmailService(token);
            ListMessagesResponse listResponse = service.users().messages()
                .list("me")
                .setMaxResults(10L)
                .execute();
                
            List<Message> messages = listResponse.getMessages();
            if (messages == null || messages.isEmpty()) {
                logger.info("No messages found for user: {}", token.getUserId());
                return new ArrayList<>();
            }
            
            logger.info("Found {} messages for user: {}", messages.size(), token.getUserId());
            
            return messages.stream()
                .map(message -> getEmailItem(service, message, token.getUserId()))
                .filter(item -> item != null)
                .toList();
                
        } catch (IOException e) {
            logger.error("Error fetching emails from Gmail for user {}: {}", token.getUserId(), e.getMessage());
            
            // Check if it's an authentication error
            if (e.getMessage().contains("401") || e.getMessage().contains("Invalid Credentials") || 
                e.getMessage().contains("UNAUTHENTICATED") || e.getMessage().contains("authError")) {
                throw new GmailAuthenticationException("Gmail authentication failed: " + e.getMessage(), e);
            }
            
            throw new RuntimeException("Failed to fetch emails from Gmail: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error for user {}: {}", token.getUserId(), e.getMessage());
            throw new RuntimeException("Failed to fetch emails: " + e.getMessage());
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
    
    private Gmail getGmailService(Token token) {
        try {
            if (token.getAccessToken() == null || token.getAccessToken().equals("gmail_access_granted")) {
                throw new IllegalStateException("No valid access token available for user: " + token.getUserId());
            }
            
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            
            Date expiryTime = new Date(System.currentTimeMillis() + (24 * 60 * 60 * 1000));
            AccessToken accessToken = new AccessToken(token.getAccessToken(), expiryTime);
            
            GoogleCredentials credentials = new GoogleCredentials(accessToken) {
                @Override
                public AccessToken refreshAccessToken() throws IOException {
                    logger.debug("Access token refresh requested for user: {}, using existing token", token.getUserId());
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
