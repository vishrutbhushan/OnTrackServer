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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
            
            // Build query to fetch emails newer than the last processed email
            String query = "";
            int maxResults = 10; // Default: fetch last 10 for first time
            
            // If we have a last processed email time, fetch only newer ones
            if (user.getUserConfig() != null && user.getUserConfig().getLastProcessedEmailTime() != null) {
                LocalDateTime lastTime = user.getUserConfig().getLastProcessedEmailTime();
                // Format: after:2023/12/25 for Gmail API
                String dateStr = lastTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
                query = "after:" + dateStr;
                maxResults = 50; // Fetch more after first run to catch up
                log.info("Fetching emails after: {} (subsequent run)", dateStr);
            } else {
                log.info("First run - fetching last 10 emails");
            }
            
            // Fetch emails
            ListMessagesResponse listResponse = service.users().messages()
                .list("me")
                .setMaxResults((long)maxResults)
                .setQ(query)
                .execute();
                
            List<Message> messages = listResponse.getMessages();
            if (messages == null || messages.isEmpty()) {
                log.info("No new messages found for user: {}", user.getUserId());
                return new ArrayList<>();
            }
            
            log.info("Found {} messages from Gmail API for user: {}", messages.size(), user.getUserId());
            
            // Reverse to process oldest first
            java.util.Collections.reverse(messages);
            
            List<Item> newEmails = new ArrayList<>();
            
            for (Message message : messages) {
                Item emailItem = getEmailItem(service, message, user.getUserId());
                if (emailItem != null) {
                    // Check if this email already exists in the database by Gmail message ID
                    if (!itemRepository.existsByGmailMessageId(emailItem.getGmailMessageId())) {
                        newEmails.add(emailItem);
                    } else {
                        log.debug("Email with message ID {} already processed, skipping", emailItem.getGmailMessageId());
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
                .setFormat("full")  // Changed from "metadata" to "full" to fetch complete email body
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
            
            // Extract and set the full email body
            String body = extractEmailBody(fullMessage);
            item.setBody(body != null ? body : "");
            
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
    
    private String extractEmailBody(Message message) {
        try {
            if (message.getPayload() == null) {
                return "";
            }
            
            // Try to get body from payload
            if (message.getPayload().getBody() != null && message.getPayload().getBody().getData() != null) {
                String data = message.getPayload().getBody().getData();
                // Decode base64url encoded data
                byte[] decoded = java.util.Base64.getUrlDecoder().decode(data);
                return new String(decoded, java.nio.charset.StandardCharsets.UTF_8);
            }
            
            // If payload has parts (multipart email), extract text from parts
            if (message.getPayload().getParts() != null && !message.getPayload().getParts().isEmpty()) {
                for (com.google.api.services.gmail.model.MessagePart part : message.getPayload().getParts()) {
                    // Look for plain text part
                    if (part.getMimeType() != null && part.getMimeType().equals("text/plain")) {
                        if (part.getBody() != null && part.getBody().getData() != null) {
                            String data = part.getBody().getData();
                            byte[] decoded = java.util.Base64.getUrlDecoder().decode(data);
                            return new String(decoded, java.nio.charset.StandardCharsets.UTF_8);
                        }
                    }
                }
                
                // If no plain text found, try HTML
                for (com.google.api.services.gmail.model.MessagePart part : message.getPayload().getParts()) {
                    if (part.getMimeType() != null && part.getMimeType().equals("text/html")) {
                        if (part.getBody() != null && part.getBody().getData() != null) {
                            String data = part.getBody().getData();
                            byte[] decoded = java.util.Base64.getUrlDecoder().decode(data);
                            return new String(decoded, java.nio.charset.StandardCharsets.UTF_8);
                        }
                    }
                }
            }
            
            return "";
        } catch (Exception e) {
            log.warn("Error extracting email body: {}", e.getMessage());
            return "";
        }
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
