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
    
    /**
     * Fetch emails from Gmail using stored OAuth tokens
     */
    public List<Item> fetchEmailsFromGmail(Token token) {
        logger.info("Fetching emails from Gmail for user: {}", token.getUserId());
        List<Item> items = new ArrayList<>();
        
        try {
            Gmail service = getGmailService(token);
            if (service == null) {
                logger.error("Failed to create Gmail service for user: {}", token.getUserId());
                throw new RuntimeException("Unable to create Gmail service. Please check your OAuth tokens.");
            }
            
            // List messages
            ListMessagesResponse listResponse = service.users().messages()
                .list("me")
                .setMaxResults(10L)
                .execute();
                
            List<Message> messages = listResponse.getMessages();
            if (messages == null || messages.isEmpty()) {
                logger.info("No messages found for user: {}", token.getUserId());
                return items;
            }
            
            logger.info("Found {} messages for user: {}", messages.size(), token.getUserId());
            
            // Get details for each message
            for (Message message : messages) {
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
                                from = header.getValue();
                                // Extract just the email address if format is "Name <email>"
                                if (from.contains("<") && from.contains(">")) {
                                    from = from.substring(from.indexOf("<") + 1, from.indexOf(">"));
                                }
                            }
                        }
                    }
                    
                    Item item = new Item(subject, "Email content preview...", from, token.getUserId());
                    items.add(item);
                    
                } catch (Exception e) {
                    logger.error("Error processing message {}: {}", message.getId(), e.getMessage());
                }
            }
            
            logger.info("Successfully processed {} emails for user: {}", items.size(), token.getUserId());
            
        } catch (IOException e) {
            if (e.getMessage().contains("401") || e.getMessage().contains("unauthorized")) {
                logger.error("OAuth token expired or invalid for user {}: {}", token.getUserId(), e.getMessage());
                throw new RuntimeException("Gmail access token has expired. Please re-authorize the application.");
            } else {
                logger.error("IO error fetching emails from Gmail for user {}: {}", token.getUserId(), e.getMessage(), e);
                throw new RuntimeException("Failed to fetch emails from Gmail: " + e.getMessage());
            }
        } catch (IllegalStateException e) {
            if (e.getMessage().contains("OAuth2Credentials")) {
                logger.error("OAuth2 token refresh error for user {}: {}", token.getUserId(), e.getMessage());
                throw new RuntimeException("Gmail access token needs to be refreshed. Please re-authorize the application.");
            } else {
                logger.error("State error fetching emails from Gmail for user {}: {}", token.getUserId(), e.getMessage(), e);
                throw new RuntimeException("Failed to fetch emails from Gmail: " + e.getMessage());
            }
        } catch (Exception e) {
            logger.error("Unexpected error fetching emails from Gmail for user {}: {}", token.getUserId(), e.getMessage(), e);
            throw new RuntimeException("Failed to fetch emails from Gmail: " + e.getMessage());
        }
        
        return items;
    }
    
    /**
     * Create Gmail service using stored OAuth tokens
     */
    private Gmail getGmailService(Token token) {
        try {
            if (token.getAccessToken() == null || token.getAccessToken().equals("gmail_access_granted")) {
                logger.error("No valid access token available for user: {}", token.getUserId());
                return null;
            }
            
            final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            
            // Create credential from stored token
            // Set expiry time far in the future to avoid automatic refresh attempts
            Date expiryTime = new Date(System.currentTimeMillis() + (24 * 60 * 60 * 1000)); // 24 hours
            AccessToken accessToken = new AccessToken(token.getAccessToken(), expiryTime);
            
            // Create credentials that won't attempt to refresh
            GoogleCredentials credentials = new GoogleCredentials(accessToken) {
                @Override
                public AccessToken refreshAccessToken() throws IOException {
                    // Don't attempt to refresh - just return the current token
                    logger.debug("Access token refresh requested for user: {}, using existing token", token.getUserId());
                    return getAccessToken();
                }
            };
            
            return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                .setApplicationName(APPLICATION_NAME)
                .build();
                
        } catch (GeneralSecurityException | IOException e) {
            logger.error("Error creating Gmail service: {}", e.getMessage(), e);
            return null;
        }
    }
}
