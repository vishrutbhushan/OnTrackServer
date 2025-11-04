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
import com.project.onTrackServer.model.User;
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
    
    /**
     * Simple email data holder for processing (not persisted to database)
     */
    public static class EmailData {
        public String messageId;
        public String subject;
        public String sender;
        public String snippet;
        public String body;
        
        public EmailData(String messageId, String subject, String sender, String snippet, String body) {
            this.messageId = messageId;
            this.subject = subject;
            this.sender = sender;
            this.snippet = snippet;
            this.body = body;
        }
    }
    
    /**
     * Fetch emails from Gmail for processing without persisting to database.
     * Used by EmailProcessingSchedulerService for email analysis and order creation.
     * Only returns emails that have not been processed before (tracks by message ID).
     * 
     * @param user The user to fetch emails for
     * @return List of EmailData objects with email content
     */
    public List<EmailData> fetchEmailsForProcessing(User user) {
        log.info("Fetching emails from Gmail for processing: {}", user.getUserId());
        
        try {
            Gmail service = getGmailService(user);
            int maxResults = 10; // Fetch more to find new ones
            
            // If we have a last processed email ID, only fetch newer emails
            String lastProcessedId = null;
            if (user.getUserConfig() != null && user.getUserConfig().getLastProcessedEmailId() != null) {
                lastProcessedId = user.getUserConfig().getLastProcessedEmailId();
                log.info("Fetching NEW emails after message ID: {} (subsequent run)", lastProcessedId);
            } else {
                log.info("First run - fetching last {} emails", maxResults);
            }
            
            ListMessagesResponse listResponse = service.users().messages()
                .list("me")
                .setMaxResults((long) maxResults)
                .execute();
            
            List<Message> messages = listResponse.getMessages();
            if (messages == null || messages.isEmpty()) {
                log.info("No messages found for user: {}", user.getUserId());
                return new ArrayList<>();
            }
            
            log.info("Found {} messages for user: {} - filtering for unprocessed ones", messages.size(), user.getUserId());
            
            // Gmail returns emails in newest-first order (reverse chronological)
            // We need to collect all emails BEFORE the lastProcessedId (newer than it)
            List<EmailData> emailDataList = new ArrayList<>();
            
            if (lastProcessedId == null) {
                // First run - process all fetched emails
                for (Message message : messages) {
                    EmailData emailData = extractEmailData(service, message);
                    if (emailData != null) {
                        emailDataList.add(emailData);
                        log.debug("Added email for processing (first run): {} from {}", emailData.messageId, emailData.sender);
                    }
                }
            } else {
                // Subsequent runs - only add emails that come BEFORE lastProcessedId in the list
                // (which means they are NEWER, since Gmail returns newest first)
                for (Message message : messages) {
                    if (message.getId().equals(lastProcessedId)) {
                        // We've reached the last processed email, stop here
                        log.debug("Reached last processed message ID: {}, stopping collection", lastProcessedId);
                        break;
                    }
                    
                    EmailData emailData = extractEmailData(service, message);
                    if (emailData != null) {
                        emailDataList.add(emailData);
                        log.debug("Added NEW email for processing: {} from {}", emailData.messageId, emailData.sender);
                    }
                }
                
                // If we never found lastProcessedId, it means it's older than our fetch
                // In this case, all fetched emails are newer, so they're all new
                if (emailDataList.size() == messages.size()) {
                    log.warn("Last processed email ID {} not found in fetched messages (likely archived/deleted), treating all {} fetched messages as new", 
                        lastProcessedId, messages.size());
                }
            }
            
            log.info("Returning {} unprocessed emails for user: {}", emailDataList.size(), user.getUserId());
            return emailDataList;
            
        } catch (IOException e) {
            log.error("Error fetching emails from Gmail for user {}: {}", user.getUserId(), e.getMessage());
            throw new RuntimeException("Failed to fetch emails from Gmail: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error fetching emails for user {}: {}", user.getUserId(), e.getMessage());
            throw new RuntimeException("Failed to fetch emails: " + e.getMessage());
        }
    }
    
    private EmailData extractEmailData(Gmail service, Message message) {
        try {
            Message fullMessage = service.users().messages()
                .get("me", message.getId())
                .setFormat("full")
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
            
            String snippet = fullMessage.getSnippet() != null ? fullMessage.getSnippet() : "";
            String body = extractEmailBody(fullMessage);
            
            return new EmailData(message.getId(), subject, from, snippet, body != null ? body : "");
            
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
                byte[] decoded = java.util.Base64.getUrlDecoder().decode(data);
                return new String(decoded, java.nio.charset.StandardCharsets.UTF_8);
            }
            
            // If payload has parts (multipart email), extract text from parts
            if (message.getPayload().getParts() != null && !message.getPayload().getParts().isEmpty()) {
                for (com.google.api.services.gmail.model.MessagePart part : message.getPayload().getParts()) {
                    if (part.getMimeType() != null && part.getMimeType().equals("text/plain")) {
                        if (part.getBody() != null && part.getBody().getData() != null) {
                            String data = part.getBody().getData();
                            byte[] decoded = java.util.Base64.getUrlDecoder().decode(data);
                            return new String(decoded, java.nio.charset.StandardCharsets.UTF_8);
                        }
                    }
                }
                
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
