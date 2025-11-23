package com.project.onTrackServer.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.*;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.http.HttpCredentialsAdapter;
import com.project.onTrackServer.model.User;
import com.project.onTrackServer.model.UserConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Service for Gmail API integration to fetch and manage emails
 */
public class EmailService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    
    private static final String APPLICATION_NAME = "OnTrack Email Processor";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Arrays.asList(
        GmailScopes.GMAIL_READONLY,
        GmailScopes.GMAIL_MODIFY
    );
    
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private final NetHttpTransport httpTransport;
    
    public EmailService() throws GeneralSecurityException, IOException {
        this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    }

    /**
     * Creates an authorized Credential object for a user.
     * @param user The user to authorize
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private Credential getCredentials(User user) throws IOException {
        // Load client secrets
        InputStream in = EmailService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        // OAuth authorization placeholder - requires proper OAuth2 dependencies
        logger.info("OAuth authorization would be performed here for user: {}", user.getUserId());
        logger.warn("OAuth2 dependencies not included for simplified build");
        return null; // Placeholder return
    }

    /**
     * Create Gmail service using user's access token
     */
    private Gmail createGmailService(User user) throws IOException, GeneralSecurityException {
        try {
            // Create credentials from user's access token
            AccessToken accessToken = new AccessToken(user.getAccessToken(), null);
            GoogleCredentials credentials = GoogleCredentials.create(accessToken);
            
            return new Gmail.Builder(httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (Exception e) {
            logger.warn("Failed to create service with stored token for user {}, attempting re-authorization", user.getUserId());
            // Fallback to credential flow
            Credential credential = getCredentials(user);
            return new Gmail.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        }
    }
    
    /**
     * Fetch emails for processing from Gmail
     * @param user The user whose emails to fetch
     * @return List of EmailData objects
     */
    public List<EmailData> fetchEmailsForProcessing(User user) {
        List<EmailData> emailDataList = new ArrayList<>();
        
        try {
            Gmail service = createGmailService(user);
            
            // Build query for filtering emails
            StringBuilder queryBuilder = new StringBuilder();
            
            // Get timestamp from user config for server-side filtering
            LocalDateTime lastProcessedTime = getLastProcessedEmailTime(user);
            if (lastProcessedTime != null) {
                ZonedDateTime zonedDateTime = lastProcessedTime.atZone(ZoneId.systemDefault());
                long epochSeconds = zonedDateTime.toEpochSecond();
                queryBuilder.append("after:").append(epochSeconds).append(" ");
            }
            
            // Add filters for common e-commerce patterns
            queryBuilder.append("(")
                      .append("subject:order OR subject:shipped OR subject:delivery OR subject:confirmation OR ")
                      .append("from:noreply OR from:orders OR from:shipping")
                      .append(") -label:spam -label:trash");
            
            String query = queryBuilder.toString();
            logger.debug("Gmail query for user {}: {}", user.getUserId(), query);
            
            // List messages
            Gmail.Users.Messages.List request = service.users().messages().list("me").setQ(query).setMaxResults(50L);
            ListMessagesResponse response = request.execute();
            
            if (response.getMessages() == null || response.getMessages().isEmpty()) {
                logger.debug("No messages found for user: {}", user.getUserId());
                return emailDataList;
            }
            
            logger.info("Found {} messages for user: {}", response.getMessages().size(), user.getUserId());
            
            // Process each message
            for (Message message : response.getMessages()) {
                try {
                    EmailData emailData = processMessage(service, message.getId());
                    if (emailData != null) {
                        emailDataList.add(emailData);
                    }
                } catch (Exception e) {
                    logger.warn("Failed to process message {} for user {}: {}", message.getId(), user.getUserId(), e.getMessage());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error fetching emails for user {}: {}", user.getUserId(), e.getMessage(), e);
        }
        
        return emailDataList;
    }
    
    /**
     * Process individual Gmail message
     */
    private EmailData processMessage(Gmail service, String messageId) throws IOException {
        Message message = service.users().messages().get("me", messageId).execute();
        
        EmailData emailData = new EmailData();
        emailData.messageId = messageId;
        emailData.snippet = message.getSnippet();
        
        MessagePart payload = message.getPayload();
        if (payload != null && payload.getHeaders() != null) {
            for (MessagePartHeader header : payload.getHeaders()) {
                switch (header.getName().toLowerCase()) {
                    case "subject":
                        emailData.subject = header.getValue();
                        break;
                    case "from":
                        emailData.sender = extractEmailFromHeader(header.getValue());
                        break;
                    case "date":
                        emailData.receivedDate = header.getValue();
                        break;
                }
            }
        }
        
        // Extract email body
        emailData.body = extractEmailBody(payload);
        
        logger.debug("Processed email - Subject: {}, From: {}", emailData.subject, emailData.sender);
        return emailData;
    }
    
    /**
     * Extract email address from header value
     */
    private String extractEmailFromHeader(String headerValue) {
        if (headerValue == null) return null;
        
        // Pattern to extract email from "Name <email@domain.com>" format
        Pattern pattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        java.util.regex.Matcher matcher = pattern.matcher(headerValue);
        
        if (matcher.find()) {
            return matcher.group();
        }
        
        return headerValue; // Return as-is if no email pattern found
    }
    
    /**
     * Extract body text from email payload
     */
    private String extractEmailBody(MessagePart payload) {
        StringBuilder bodyBuilder = new StringBuilder();
        
        if (payload.getParts() == null || payload.getParts().isEmpty()) {
            // Single part message
            if (payload.getBody() != null && payload.getBody().getData() != null) {
                byte[] data = Base64.getUrlDecoder().decode(payload.getBody().getData());
                bodyBuilder.append(new String(data));
            }
        } else {
            // Multi-part message
            extractBodyFromParts(payload.getParts(), bodyBuilder);
        }
        
        return bodyBuilder.toString();
    }
    
    /**
     * Recursively extract body from message parts
     */
    private void extractBodyFromParts(List<MessagePart> parts, StringBuilder bodyBuilder) {
        for (MessagePart part : parts) {
            if (part.getParts() != null && !part.getParts().isEmpty()) {
                // Nested parts
                extractBodyFromParts(part.getParts(), bodyBuilder);
            } else if (part.getMimeType() != null) {
                if (part.getMimeType().equals("text/plain") || part.getMimeType().equals("text/html")) {
                    if (part.getBody() != null && part.getBody().getData() != null) {
                        byte[] data = Base64.getUrlDecoder().decode(part.getBody().getData());
                        bodyBuilder.append(new String(data)).append("\n");
                    }
                }
            }
        }
    }
    
    /**
     * Archive an email by removing it from inbox
     */
    public void archiveEmail(User user, String messageId) {
        try {
            Gmail service = createGmailService(user);
            
            ModifyMessageRequest modifyRequest = new ModifyMessageRequest()
                    .setRemoveLabelIds(Collections.singletonList("INBOX"));
            
            service.users().messages().modify("me", messageId, modifyRequest).execute();
            logger.debug("Archived email {} for user {}", messageId, user.getUserId());
            
        } catch (Exception e) {
            logger.error("Failed to archive email {} for user {}: {}", messageId, user.getUserId(), e.getMessage());
        }
    }
    
    /**
     * Get last processed email timestamp from user config
     */
    private LocalDateTime getLastProcessedEmailTime(User user) {
        try {
            UserConfig config = getUserConfig(user);
            return config != null ? config.getLastProcessedEmailTime() : null;
        } catch (Exception e) {
            logger.warn("Could not get last processed email time for user {}: {}", user.getUserId(), e.getMessage());
            return null;
        }
    }
    
    /**
     * Get user config - placeholder for actual implementation
     */
    private UserConfig getUserConfig(User user) {
        // This should be implemented based on your UserConfig model
        // For now, returning null to avoid compilation errors
        return null;
    }
    
    /**
     * Shutdown service
     */
    public void shutdown() {
        // Clean up resources if needed
        logger.info("EmailService shutdown completed");
    }
    
    /**
     * Data class for email information
     */
    public static class EmailData {
        public String messageId;
        public String subject;
        public String sender;
        public String body;
        public String snippet;
        public String receivedDate;
    }
}
