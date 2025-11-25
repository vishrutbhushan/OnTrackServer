package com.project.onTrackServer.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.*;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.project.onTrackServer.Models.User;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

@Data
public class Email {

    private static final Logger logger = LoggerFactory.getLogger(Email.class);

    private String id;
    private String from;
    private String subject;
    private String body;
    private String date;
    private User user;

    public static List<Email> fetchEmails(User user) {
        List<Email> emails = new ArrayList<>();

        try {
            Gmail service = createGmailService(user);
            if (service == null) {
                logger.error("Could not create Gmail service for user: {}", user.getEmail());
                return emails;
            }

            String query = buildQuery(user);

            // Fetch list of message IDs
            ListMessagesResponse response = service.users().messages()
                    .list("me")
                    .setQ(query)
                    .setMaxResults(30L)
                    .execute();

            if (response.getMessages() == null)
                return emails;

            
            for (Message message : response.getMessages()) {
                Email email = readMessage(service, message.getId());
                if (email != null) {
                    email.setUser(user);
                    emails.add(email);
                }
            }

        } catch (Exception e) {
            logger.error("Error fetching emails for user {}: {}", user.getEmail(), e.getMessage());
        }

        return emails;
    }

    private static Gmail createGmailService(User user) throws IOException, GeneralSecurityException {
        AccessToken token = new AccessToken(user.getAccessToken(), null);
        GoogleCredentials credentials = GoogleCredentials.create(token);

        return new Gmail.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("onTrackServer")
                .build();
    }

    private static Email readMessage(Gmail service, String messageId) {
        try {
            
            Message message = service.users().messages().get("me", messageId).setFormat("full").execute();

            Email email = new Email();
            email.setId(messageId);
            email.setBody(extractBody(message.getPayload()));

            
            if (message.getPayload() != null && message.getPayload().getHeaders() != null) {
                for (MessagePartHeader header : message.getPayload().getHeaders()) {
                    String name = header.getName().toLowerCase();
                    switch (name) {
                        case "from" -> email.setFrom(header.getValue());
                        case "subject" -> email.setSubject(header.getValue());
                        case "date" -> email.setDate(header.getValue());
                    }
                }
            }

            
            return email;
        } catch (Exception e) {
            logger.error("Error reading message {}: {}", messageId, e.getMessage());
            return null;
        }
    }

    private static String extractBody(MessagePart payload) {
        if (payload == null)
            return "";

        if (payload.getBody() != null && payload.getBody().getData() != null) {
            return decode(payload.getBody().getData());
        }

        if (payload.getParts() != null) {
            for (MessagePart part : payload.getParts()) {
                if ("text/plain".equalsIgnoreCase(part.getMimeType()) && part.getBody().getData() != null) {
                    return decode(part.getBody().getData());
                }
                String recursiveBody = extractBody(part);
                if (!recursiveBody.isEmpty()) {
                    return recursiveBody;
                }
            }
        }
        return "";
    }

    private static String decode(String data) {
        return new String(Base64.getUrlDecoder().decode(data), StandardCharsets.UTF_8);
    }

    private static String buildQuery(User user) {
        StringBuilder query = new StringBuilder(
                "(order OR shipped OR delivery OR confirmation) -spam -trash");

        
        if (user.getUserConfig() != null && user.getUserConfig().getLastProcessedEmailTime() != null) {
            
            ZonedDateTime zdt = user.getUserConfig().getLastProcessedEmailTime().atZone(ZoneId.systemDefault());
            query.append(" after:").append(zdt.toEpochSecond());
        }

        return query.toString();
    }

    public static List<Email> fetchEmailsForProcessing(User user) {
        return fetchEmails(user);
    }

    public void archive() {
       
        try {
            Gmail service = createGmailService(user);
            if (service == null) {
                logger.error("Could not create Gmail service for user: {}", user.getEmail());
                return;
            }

            ModifyMessageRequest modifyMessageRequest = new ModifyMessageRequest()
                    .setRemoveLabelIds(Arrays.asList("INBOX")); 

            service.users().messages().modify("me", this.id, modifyMessageRequest).execute();
            logger.info("Archived email with ID: {}", this.id);

        } catch (Exception e) {
            logger.error("Error archiving email ");
        }
    }
}
