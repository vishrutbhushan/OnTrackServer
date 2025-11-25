package com.project.onTrackServer.Models;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

import java.io.InputStream;

public class Notifications {

    private final User user;
    private final Order order;

    static {
        initializeFirebase();
    }

    public Notifications(User user, Order order) {
        this.user = user;
        this.order = order;
    }

    private static void initializeFirebase() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                InputStream serviceAccount = Notifications.class.getClassLoader()
                        .getResourceAsStream("service-account-key.json");

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }

    public void sendOrderNotification() {
        String status = (order.getShipmentStatus() != null) ? order.getShipmentStatus() : "Order Update";
        String title = status.toUpperCase();
        String body = "Order " + order.getOrderId() + " (" + order.getQuantity() + " item(s)) - " + status;

        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Message message = Message.builder()
                .setToken(user.getFcmToken())
                .setNotification(notification)
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send notification", e);
        }
    }
}
