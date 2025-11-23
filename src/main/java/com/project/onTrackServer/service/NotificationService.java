package com.project.onTrackServer.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.auth.oauth2.GoogleCredentials;
import com.project.onTrackServer.model.User;
import com.project.onTrackServer.model.UserConfig;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class NotificationService {

    private String projectId;

    private FirebaseApp firebaseApp;

    public NotificationService() {
        loadConfiguration();
        initializeFirebase();
    }

    private void loadConfiguration() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                Properties props = new Properties();
                props.load(input);
                this.projectId = props.getProperty("google.cloud.project-id");

                if (projectId == null || projectId.isEmpty()) {
                }
            }
        } catch (IOException e) {
        }
    }

    public void initializeFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {

                try (InputStream serviceAccountStream = getClass().getClassLoader()
                        .getResourceAsStream("service-account-key.json")) {

                    if (serviceAccountStream == null) {
                        throw new RuntimeException("Firebase service account key not found");
                    }

                    FirebaseOptions options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccountStream))
                            .setProjectId(projectId)
                            .build();

                    firebaseApp = FirebaseApp.initializeApp(options);

                }
            } else {
                firebaseApp = FirebaseApp.getInstance();

            }
        } catch (IOException e) {

            throw new RuntimeException("Firebase initialization failed", e);
        }
    }

    public void sendNotification(String userId, String title, String messageBody) {
        try {

            User userModel = new User();
            User user = userModel.findByUserId(userId);

            if (user == null) {
                throw new RuntimeException("User not found: " + userId);
            }

            UserConfig userConfig = getUserConfig(user);
            if (userConfig != null) {
                Boolean notificationsEnabled = userConfig.getNotificationEnabled();
                if (notificationsEnabled != null && !notificationsEnabled) {
                    return;
                }
            }

            if (user.getFcmToken() == null) {
                throw new RuntimeException("No FCM token found for user: " + userId);
            }

            String fcmToken = user.getFcmToken();

            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(messageBody)
                    .build();

            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(notification)
                    .putData("userId", userId)
                    .putData("timestamp", String.valueOf(System.currentTimeMillis()))
                    .build();

            FirebaseMessaging.getInstance(firebaseApp).send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send notification: " + e.getMessage(), e);
        }
    }

    public static class NotificationTemplates {

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

        public NotificationTemplate getTemplate(String status, String orderId, String productName) {
            OrderStatus orderStatus = OrderStatus.fromCode(status);

            if (orderStatus == null) {
                return getDefaultTemplate(orderId, productName);
            }

            return switch (orderStatus) {
                case ORDERED -> getOrderedTemplate(orderId, productName);
                case SHIPPED -> getShippedTemplate(orderId, productName);
                case OUT_OF_DELIVERY -> getOutOfDeliveryTemplate(orderId, productName);
                case DELIVERED -> getDeliveredTemplate(orderId, productName);
                case CANCELLED -> getCancelledTemplate(orderId, productName);
            };
        }

        private NotificationTemplate getOrderedTemplate(String orderId, String productName) {
            String title = "Order Confirmed";
            String body = "Your order " + orderId + " has been confirmed!\n" +
                    "Product: " + (productName != null ? productName : "Your item");
            return new NotificationTemplate(title, body);
        }

        private NotificationTemplate getShippedTemplate(String orderId, String productName) {
            String title = "Order Shipped";
            String body = "Your order " + orderId + " is on its way!\n" +
                    "Item: " + (productName != null ? productName : "Your item");
            return new NotificationTemplate(title, body);
        }

        private NotificationTemplate getOutOfDeliveryTemplate(String orderId, String productName) {
            String title = "Out for Delivery";
            String body = "Great news! Order " + orderId + " is out for delivery today.\n" +
                    "Item: " + (productName != null ? productName : "Your item");
            return new NotificationTemplate(title, body);
        }

        private NotificationTemplate getDeliveredTemplate(String orderId, String productName) {
            String title = "Order Delivered";
            String body = "Your order " + orderId + " has been delivered!\n" +
                    "Item: " + (productName != null ? productName : "Your item");
            return new NotificationTemplate(title, body);
        }

        private NotificationTemplate getCancelledTemplate(String orderId, String productName) {
            String title = "Order Cancelled";
            String body = "Order " + orderId + " has been cancelled.\n" +
                    "Item: " + (productName != null ? productName : "Your item");
            return new NotificationTemplate(title, body);
        }

        private NotificationTemplate getDefaultTemplate(String orderId, String productName) {
            String title = "Order Update";
            String body = "Order " + orderId + " update:\n" +
                    "Item: " + (productName != null ? productName : "Your item") + "\n" +
                    "Check your order status anytime.";
            return new NotificationTemplate(title, body);
        }

    }

    private UserConfig getUserConfig(User user) {
        try {
            UserConfig config = new UserConfig();

            config.setNotificationEnabled(true);
            return config;
        } catch (Exception e) {
            return null;
        }
    }
}
