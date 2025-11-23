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

public class EmailService {
    
    
    private static final String APPLICATION_NAME = "OnTrack Email Processor";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private static final List<String> SCOPES = Arrays.asList(
        GmailScopes.GMAIL_READONLY,
        GmailScopes.GMAIL_MODIFY
    );
    
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private final NetHttpTransport httpTransport;
    
    public EmailService() throws GeneralSecurityException, IOException {
        this.httpTransport = GoogleNetHttpTransport.newTrustedTransport();
    }

    
    private Credential getCredentials(User user) throws IOException {
        
        InputStream in = EmailService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        return null;
    }

    
    private Gmail createGmailService(User user) throws IOException, GeneralSecurityException {
        try {
            
            AccessToken accessToken = new AccessToken(user.getAccessToken(), null);
            GoogleCredentials credentials = GoogleCredentials.create(accessToken);
            
            return new Gmail.Builder(httpTransport, JSON_FACTORY, new HttpCredentialsAdapter(credentials))
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (Exception e) {
            
            Credential credential = getCredentials(user);
            return new Gmail.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        }
    }
    
    
    public List<EmailData> fetchEmailsForProcessing(User user) {
        List<EmailData> emailDataList = new ArrayList<>();
        
        try {
            Gmail service = createGmailService(user);
            
            
            StringBuilder queryBuilder = new StringBuilder();
            
            
            LocalDateTime lastProcessedTime = getLastProcessedEmailTime(user);
            if (lastProcessedTime != null) {
                ZonedDateTime zonedDateTime = lastProcessedTime.atZone(ZoneId.systemDefault());
                long epochSeconds = zonedDateTime.toEpochSecond();
                queryBuilder.append("after:").append(epochSeconds).append(" ");
            }
            
            
            queryBuilder.append("(")
                      .append("subject:order OR subject:shipped OR subject:delivery OR subject:confirmation OR ")
                      .append("from:noreply OR from:orders OR from:shipping")
                      .append(") -label:spam -label:trash");
            
            String query = queryBuilder.toString();
            
            
            
            Gmail.Users.Messages.List request = service.users().messages().list("me").setQ(query).setMaxResults(50L);
            ListMessagesResponse response = request.execute();
            
            if (response.getMessages() == null || response.getMessages().isEmpty()) {
                return emailDataList;
            }
            
            
            
            
            for (Message message : response.getMessages()) {
                try {
                    EmailData emailData = processMessage(service, message.getId());
                    if (emailData != null) {
                        emailDataList.add(emailData);
                    }
                    } catch (Exception e) {
                    
                }
            }
            
        } catch (Exception e) {
            
        }
        
        return emailDataList;
    }
    
    
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
        
        
        emailData.body = extractEmailBody(payload);
        
        
        return emailData;
    }
    
    
    private String extractEmailFromHeader(String headerValue) {
        if (headerValue == null) return null;
        
        
        Pattern pattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        java.util.regex.Matcher matcher = pattern.matcher(headerValue);
        
        if (matcher.find()) {
            return matcher.group();
        }
        
        return headerValue;
    }
    
    
    private String extractEmailBody(MessagePart payload) {
        StringBuilder bodyBuilder = new StringBuilder();
        
        if (payload.getParts() == null || payload.getParts().isEmpty()) {
            if (payload.getBody() != null && payload.getBody().getData() != null) {
                byte[] data = Base64.getUrlDecoder().decode(payload.getBody().getData());
                bodyBuilder.append(new String(data));
            }
        } else {
            
            extractBodyFromParts(payload.getParts(), bodyBuilder);
        }
        
        return bodyBuilder.toString();
    }
    
    
    private void extractBodyFromParts(List<MessagePart> parts, StringBuilder bodyBuilder) {
        for (MessagePart part : parts) {
            if (part.getParts() != null && !part.getParts().isEmpty()) {
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
    
    
    public void archiveEmail(User user, String messageId) {
        try {
            Gmail service = createGmailService(user);
            
            ModifyMessageRequest modifyRequest = new ModifyMessageRequest()
                    .setRemoveLabelIds(Collections.singletonList("INBOX"));
            
            service.users().messages().modify("me", messageId, modifyRequest).execute();
            
        } catch (Exception e) {
            
        }
    }
    
    
    private LocalDateTime getLastProcessedEmailTime(User user) {
        try {
            UserConfig config = getUserConfig(user);
            return config != null ? config.getLastProcessedEmailTime() : null;
        } catch (Exception e) {
            
            return null;
        }
    }
    
    
    private UserConfig getUserConfig(User user) {
        
        return null;
    }
    
    
    public void shutdown() {
        
    }
    
    
    public static class EmailData {
        public String messageId;
        public String subject;
        public String sender;
        public String body;
        public String snippet;
        public String receivedDate;
    }
}
