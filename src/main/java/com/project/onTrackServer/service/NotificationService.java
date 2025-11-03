package com.project.onTrackServer.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.auth.oauth2.GoogleCredentials;
import com.project.onTrackServer.model.User;
import com.project.onTrackServer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Value("${google.cloud.project-id}")
    private String projectId;
    
    private FirebaseApp firebaseApp;

    @PostConstruct
    public void initializeFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                // Load service account key from classpath
                ClassPathResource resource = new ClassPathResource("service-account-key.json");
                try (InputStream serviceAccountStream = resource.getInputStream()) {
                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                            .setProjectId(projectId)
                            .build();
                    
                    firebaseApp = FirebaseApp.initializeApp(options);
                    logger.info("Firebase Admin SDK initialized successfully for project: {}", projectId);
                }
            } else {
                firebaseApp = FirebaseApp.getInstance();
                logger.info("Firebase Admin SDK already initialized");
            }
        } catch (IOException e) {
            logger.error("Failed to initialize Firebase Admin SDK: {}", e.getMessage(), e);
            throw new RuntimeException("Firebase initialization failed", e);
        }
    }

    public void sendNotification(String userId, String title, String messageBody) {
        try {
            // Get user details
            Optional<User> userOpt = userRepository.findByUserId(userId);
            
            if (userOpt.isEmpty()) {
                logger.warn("User not found: {}", userId);
                throw new RuntimeException("User not found: " + userId);
            }
            
            User user = userOpt.get();
            
            // Check if notifications are enabled for this user
            if (user.getUserConfig() != null) {
                Boolean notificationsEnabled = user.getUserConfig().getNotificationEnabled();
                if (notificationsEnabled != null && !notificationsEnabled) {
                    logger.info("Notifications are disabled for user: {}", userId);
                    return;
                }
            }
            
            if (user.getFcmToken() == null) {
                logger.warn("No FCM token found for user: {}", userId);
                throw new RuntimeException("No FCM token found for user: " + userId);
            }
            
            String fcmToken = user.getFcmToken();
            logger.info("Sending notification to user {}, FCM token: {}...", userId, fcmToken.substring(0, Math.min(fcmToken.length(), 20)));
            
            // Build the notification
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(messageBody)
                    .build();
            
            // Build the message
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(notification)
                    .putData("userId", userId)
                    .putData("timestamp", String.valueOf(System.currentTimeMillis()))
                    .build();
            
            // Send the message
            String response = FirebaseMessaging.getInstance(firebaseApp).send(message);
            logger.info("Successfully sent notification to user {}, FCM response: {}", userId, response);
            
        } catch (Exception e) {
            logger.error("Error sending notification to user {}: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to send notification: " + e.getMessage(), e);
        }
    }
    
    public void sendTestNotification(String userId) {
        sendNotification(
            userId, 
            "OnTrack Test Notification", 
            "This is a test notification from OnTrack backend! 🚀"
        );
    }
    
    public void sendNotificationToToken(String fcmToken, String title, String messageBody) {
        try {
            logger.info("Sending notification directly to FCM token: {}...", fcmToken.substring(0, Math.min(fcmToken.length(), 20)));
            
            // Build the notification
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(messageBody)
                    .build();
            
            // Build the message
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(notification)
                    .putData("timestamp", String.valueOf(System.currentTimeMillis()))
                    .build();
            
            // Send the message
            String response = FirebaseMessaging.getInstance(firebaseApp).send(message);
            logger.info("Successfully sent notification to FCM token, response: {}", response);
            
        } catch (Exception e) {
            logger.error("Error sending notification to FCM token: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send notification: " + e.getMessage(), e);
        }
    }
}
