package com.project.onTrackServer.controller;

import com.project.onTrackServer.model.User;
import com.project.onTrackServer.model.ApiResponse;
import com.project.onTrackServer.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        try {
            List<User> users = userService.getAllActiveUsers();
            return ResponseEntity.ok(new ApiResponse<>(true, "Users retrieved successfully", users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error retrieving users: " + e.getMessage(), null));
        }
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@RequestBody CreateUserRequest request) {
        try {
            logger.info("Create user request for: {}", request.getEmail());
            User createdUser = userService.createUserWithDetails(
                request.getUserId(),
                request.getEmail(), 
                request.getName(), 
                request.getAge(),
                request.getAuthToken(),
                request.getFcmToken(),
                request.getPushNotificationEnabled(),
                request.getPollingFrequency()
            );
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "User created successfully", createdUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error creating user: " + e.getMessage(), null));
        }
    }
    
    @PutMapping
    public ResponseEntity<ApiResponse<User>> saveUser(@RequestBody User user) {
        try {
            logger.info("Save user request for: {}", user.getEmail());
            User savedUser = userService.saveUser(user);
            return ResponseEntity.ok(new ApiResponse<>(true, "User saved successfully", savedUser));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error saving user: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<User>> getUser(@PathVariable Long userId) {
        try {
            logger.info("Get user request for: {}", userId);
            return userService.getUserByUserId(userId)
                .map(user -> ResponseEntity.ok(new ApiResponse<>(true, "User found", user)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "User not found", null)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error retrieving user: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<User>> getUserByEmail(@PathVariable String email) {
        try {
            logger.info("Get user by email request for: {}", email);
            return userService.getUserByEmail(email)
                .map(user -> ResponseEntity.ok(new ApiResponse<>(true, "User found", user)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "User not found", null)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error retrieving user: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<User>>> searchUsers(@RequestParam String name) {
        try {
            List<User> users = userService.searchUsersByName(name);
            return ResponseEntity.ok(new ApiResponse<>(true, "Users found", users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error searching users: " + e.getMessage(), null));
        }
    }
    
    @PutMapping("/{userId}/auth-token")
    public ResponseEntity<ApiResponse<User>> updateAuthToken(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        try {
            logger.info("Update auth token request for: {}", userId);
            String authToken = request.get("authToken");
            User updatedUser = userService.updateAuthToken(userId, authToken);
            return ResponseEntity.ok(new ApiResponse<>(true, "Auth token updated successfully", updatedUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error updating auth token: " + e.getMessage(), null));
        }
    }
    
    @PutMapping("/{userId}/fcm-token")
    public ResponseEntity<ApiResponse<User>> updateFcmToken(
            @PathVariable Long userId,
            @RequestBody Map<String, String> request) {
        try {
            logger.info("Update FCM token request for: {}", userId);
            String fcmToken = request.get("fcmToken");
            User updatedUser = userService.updateFcmToken(userId, fcmToken);
            return ResponseEntity.ok(new ApiResponse<>(true, "FCM token updated successfully", updatedUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error updating FCM token: " + e.getMessage(), null));
        }
    }
    
    @PutMapping("/{userId}/notification-settings")
    public ResponseEntity<ApiResponse<User>> updateNotificationSettings(
            @PathVariable Long userId,
            @RequestBody NotificationSettingsRequest request) {
        try {
            logger.info("Update notification settings request for: {}", userId);
            User updatedUser = userService.updateNotificationSettings(
                    userId, 
                    request.getPushNotificationEnabled(), 
                    request.getPollingFrequency()
            );
            return ResponseEntity.ok(new ApiResponse<>(true, "Notification settings updated successfully", updatedUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error updating notification settings: " + e.getMessage(), null));
        }
    }
    
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long userId, 
            @RequestBody DeleteUserRequest request) {
        try {
            logger.info("Delete user request for: {}", userId);
            userService.deleteUser(userId, request.getDeleteUserId());
            return ResponseEntity.ok(new ApiResponse<>(true, "User deleted successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error deleting user: " + e.getMessage(), null));
        }
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        logger.error("Error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ApiResponse<>(false, e.getMessage(), null));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        logger.error("Unexpected error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiResponse<>(false, "Internal server error: " + e.getMessage(), null));
    }
    
    // Request DTOs
    public static class CreateUserRequest {
        private String userId;
        private String email;
        private String name;
        private Integer age;
        private String authToken;
        private String fcmToken;
        private Boolean pushNotificationEnabled;
        private User.PollingFrequency pollingFrequency;
        
        // Getters and Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        
        public String getAuthToken() { return authToken; }
        public void setAuthToken(String authToken) { this.authToken = authToken; }
        
        public String getFcmToken() { return fcmToken; }
        public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
        
        public Boolean getPushNotificationEnabled() { return pushNotificationEnabled; }
        public void setPushNotificationEnabled(Boolean pushNotificationEnabled) { 
            this.pushNotificationEnabled = pushNotificationEnabled; 
        }
        
        public User.PollingFrequency getPollingFrequency() { return pollingFrequency; }
        public void setPollingFrequency(User.PollingFrequency pollingFrequency) { 
            this.pollingFrequency = pollingFrequency; 
        }
    }
    
    public static class NotificationSettingsRequest {
        private Boolean pushNotificationEnabled;
        private User.PollingFrequency pollingFrequency;
        
        // Getters and Setters
        public Boolean getPushNotificationEnabled() { return pushNotificationEnabled; }
        public void setPushNotificationEnabled(Boolean pushNotificationEnabled) { 
            this.pushNotificationEnabled = pushNotificationEnabled; 
        }
        
        public User.PollingFrequency getPollingFrequency() { return pollingFrequency; }
        public void setPollingFrequency(User.PollingFrequency pollingFrequency) { 
            this.pollingFrequency = pollingFrequency; 
        }
    }
    
    public static class DeleteUserRequest {
        private Long deleteUserId;
        
        // Getters and Setters
        public Long getDeleteUserId() { return deleteUserId; }
        public void setDeleteUserId(Long deleteUserId) { this.deleteUserId = deleteUserId; }
    }
}
