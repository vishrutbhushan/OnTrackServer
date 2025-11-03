package com.project.onTrackServer.controller;

import com.project.onTrackServer.dto.ApiResponse;
import com.project.onTrackServer.dto.UserDTO;
import com.project.onTrackServer.model.User;
import com.project.onTrackServer.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired
    private UserService userService;
    
    @PostMapping
    public ResponseEntity<UserDTO> saveUser(@RequestBody UserDTO userDTO) {
        logger.info("Save user request for: {}", userDTO.getUserId());
        UserDTO savedUser = userService.saveUser(userDTO);
        return ResponseEntity.ok(savedUser);
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUser(@PathVariable String userId) {
        logger.info("Get user request for: {}", userId);
        return userService.getUserByUserId(userId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<User> getUserByEmail(@PathVariable String email) {
        logger.info("Get user by email request for: {}", email);
        return userService.getUserByEmail(email)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{userId}/access-token")
    public ResponseEntity<UserDTO> updateAccessToken(
            @PathVariable String userId,
            @RequestBody Map<String, String> request) {
        logger.info("Update access token request for: {}", userId);
        String accessToken = request.get("accessToken");
        UserDTO updatedUser = userService.updateAccessToken(userId, accessToken);
        return ResponseEntity.ok(updatedUser);
    }
    
    @PutMapping("/{userId}/fcm-token")
    public ResponseEntity<UserDTO> updateFcmToken(
            @PathVariable String userId,
            @RequestBody Map<String, String> request) {
        logger.info("Update FCM token request for: {}", userId);
        String fcmToken = request.get("fcmToken");
        UserDTO updatedUser = userService.updateFcmToken(userId, fcmToken);
        return ResponseEntity.ok(updatedUser);
    }
    
    @PutMapping("/{userId}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable String userId,
            @RequestBody UserDTO userDTO) {
        logger.info("Update user request for: {}", userId);
        userDTO.setUserId(userId);
        UserDTO updatedUser = userService.saveUser(userDTO);
        return ResponseEntity.ok(updatedUser);
    }
    
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse> deleteUser(@PathVariable String userId) {
        logger.info("Delete user request for: {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.ok(new ApiResponse(true, "User deleted successfully"));
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgument(IllegalArgumentException e) {
        logger.error("Error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(new ApiResponse(false, e.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(Exception e) {
        logger.error("Unexpected error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ApiResponse(false, "Internal server error: " + e.getMessage()));
    }
}
