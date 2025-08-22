package com.project.onTrackServer.controller;

import com.project.onTrackServer.model.ApiResponse;
import com.project.onTrackServer.model.FCMToken;
import com.project.onTrackServer.repository.FCMTokenRepository;
import com.project.onTrackServer.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * Controller for managing push notifications
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private FCMTokenRepository fcmTokenRepository;
    
    @Autowired
    private NotificationService notificationService;

    /**
     * Send a test notification to a user
     * @param userId The user ID
     * @return Response indicating success/failure
     */
    @PostMapping("/test/{userId}")
    public ResponseEntity<ApiResponse> sendTestNotification(@PathVariable String userId) {
        try {
            notificationService.sendTestNotification(userId);
            return ResponseEntity.ok(new ApiResponse(true, "Test notification sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Failed to send test notification: " + e.getMessage()));
        }
    }
    
    /**
     * Send a custom notification
     * @param userId The user ID
     * @param title Notification title
     * @param message Notification message
     * @return Response indicating success/failure
     */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse> sendCustomNotification(
            @RequestParam String userId,
            @RequestParam String title,
            @RequestParam String message) {
        try {
            notificationService.sendNotification(userId, title, message);
            return ResponseEntity.ok(new ApiResponse(true, "Notification sent successfully for user: " + userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Failed to send notification: " + e.getMessage()));
        }
    }
    
    /**
     * Simple test endpoint
     * @param userId The user ID
     * @return Response with timestamp
     */
    @PostMapping("/simple-test/{userId}")
    public ResponseEntity<ApiResponse> sendSimpleTestNotification(@PathVariable String userId) {
        return ResponseEntity.ok(new ApiResponse(true, "Simple test notification triggered for user: " + userId + " at " + java.time.LocalDateTime.now()));
    }
    
    /**
     * Store FCM token for a user
     * @param userId The user ID
     * @param fcmToken The FCM token
     * @return Response indicating success/failure
     */
    @PostMapping("/fcm-token/{userId}")
    public ResponseEntity<ApiResponse> storeFCMToken(@PathVariable String userId, @RequestParam String fcmToken) {
        try {
            Optional<FCMToken> existingToken = fcmTokenRepository.findByUserId(userId);
            
            FCMToken fcmTokenEntity;
            if (existingToken.isPresent()) {
                fcmTokenEntity = existingToken.get();
                fcmTokenEntity.setFcmToken(fcmToken);
            } else {
                fcmTokenEntity = new FCMToken(userId, fcmToken);
            }
            
            fcmTokenRepository.save(fcmTokenEntity);
            
            return ResponseEntity.ok(new ApiResponse(true, "FCM token stored successfully for user: " + userId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Failed to store FCM token: " + e.getMessage()));
        }
    }
}
