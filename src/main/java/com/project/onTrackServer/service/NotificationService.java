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


    public class NotificationTemplates {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationTemplates.class);
    
    /**
     * Enum for all possible order statuses
     */
    public enum OrderStatus {
        ORDERED("ordered", "Order Placed"),
        SHIPPED("shipped", "Order Shipped"),
        OUT_OF_DELIVERY("out_of_delivery", "Out for Delivery"),
        DELIVERED("delivered", "Order Delivered"),
        CANCELLED("cancelled", "Order Cancelled");
        
        private final String code;
        private final String displayName;
        
        OrderStatus(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public static OrderStatus fromCode(String code) {
            if (code == null) {
                return null;
            }
            for (OrderStatus status : OrderStatus.values()) {
                if (status.code.equalsIgnoreCase(code)) {
                    return status;
                }
            }
            return null;
        }
    }
    
    /**
     * Template data holder class
     */
    public static class NotificationTemplate {
        private final String title;
        private final String body;
        
        public NotificationTemplate(String title, String body) {
            this.title = title;
            this.body = body;
        }
        
        public String getTitle() {
            return title;
        }
        
        public String getBody() {
            return body;
        }
    }
    
    /**
     * Get notification template for a specific order status
     * @param status The order status
     * @param orderId The order ID
     * @param productName The product name
     * @return NotificationTemplate with title and body
     */
    public NotificationTemplate getTemplate(String status, String orderId, String productName) {
        OrderStatus orderStatus = OrderStatus.fromCode(status);
        
        if (orderStatus == null) {
            logger.warn("Unknown order status: {}, using default template", status);
            return getDefaultTemplate(orderId, productName);
        }
        
        logger.debug("Generating notification template for status: {}", orderStatus.getDisplayName());
        
        return switch (orderStatus) {
            case ORDERED -> getOrderedTemplate(orderId, productName);
            case SHIPPED -> getShippedTemplate(orderId, productName);
            case OUT_OF_DELIVERY -> getOutOfDeliveryTemplate(orderId, productName);
            case DELIVERED -> getDeliveredTemplate(orderId, productName);
            case CANCELLED -> getCancelledTemplate(orderId, productName);
        };
    }
    
    /**
     * Template: Order Placed
     */
    private NotificationTemplate getOrderedTemplate(String orderId, String productName) {
        String title = "Order Confirmed";
        String body = "Your order " + orderId + " has been confirmed!\n" +
                     "Product: " + (productName != null ? productName : "Your item");
        return new NotificationTemplate(title, body);
    }
    
    /**
     * Template: Order Shipped
     */
    private NotificationTemplate getShippedTemplate(String orderId, String productName) {
        String title = "Order Shipped";
        String body = "Your order " + orderId + " is on its way!\n" +
                     "Item: " + (productName != null ? productName : "Your item");
        return new NotificationTemplate(title, body);
    }
    
    /**
     * Template: Out for Delivery
     */
    private NotificationTemplate getOutOfDeliveryTemplate(String orderId, String productName) {
        String title = "Out for Delivery";
        String body = "Great news! Order " + orderId + " is out for delivery today.\n" +
                     "Item: " + (productName != null ? productName : "Your item");
        return new NotificationTemplate(title, body);
    }
    
    /**
     * Template: Order Delivered
     */
    private NotificationTemplate getDeliveredTemplate(String orderId, String productName) {
        String title = "Order Delivered";
        String body = "Your order " + orderId + " has been delivered!\n" +
                     "Item: " + (productName != null ? productName : "Your item");
        return new NotificationTemplate(title, body);
    }
    
    /**
     * Template: Order Cancelled
     */
    private NotificationTemplate getCancelledTemplate(String orderId, String productName) {
        String title = "Order Cancelled";
        String body = "Order " + orderId + " has been cancelled.\n" +
                     "Item: " + (productName != null ? productName : "Your item") ;
        return new NotificationTemplate(title, body);
    }
    
    /**
     * Default template for unknown status
     */
    private NotificationTemplate getDefaultTemplate(String orderId, String productName) {
        String title = "Order Update";
        String body = "Order " + orderId + " update:\n" +
                     "Item: " + (productName != null ? productName : "Your item") + "\n" +
                     "Check your order status anytime.";
        return new NotificationTemplate(title, body);
    }
}
