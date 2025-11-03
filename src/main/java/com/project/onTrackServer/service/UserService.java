package com.project.onTrackServer.service;

import com.project.onTrackServer.model.User;
import com.project.onTrackServer.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    public User saveUser(User user) {
        logger.info("Saving/updating user: {}", user.getUserId());
        
        Optional<User> existingUser = userRepository.findByUserId(user.getUserId());
        
        if (existingUser.isPresent()) {
            User userData = existingUser.get();
            userData.setEmail(user.getEmail());
            userData.setDisplayName(user.getDisplayName());
            if (user.getAccessToken() != null) {
                userData.setAccessToken(user.getAccessToken());
            }
            if (user.getFcmToken() != null) {
                userData.setFcmToken(user.getFcmToken());
            }
            return userRepository.save(userData);
        }
        
        return userRepository.save(user);
    }
    
    public Optional<User> getUserByUserId(String userId) {
        logger.info("Fetching user: {}", userId);
        return userRepository.findByUserId(userId);
    }
    
    public Optional<User> getUserByEmail(String email) {
        logger.info("Fetching user by email: {}", email);
        return userRepository.findByEmail(email);
    }
    
    public User updateAccessToken(String userId, String accessToken) {
        logger.info("Updating access token for user: {}", userId);
        
        User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        user.setAccessToken(accessToken);
        return userRepository.save(user);
    }
    
    public User updateFcmToken(String userId, String fcmToken) {
        logger.info("Updating FCM token for user: {}", userId);
        
        User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        user.setFcmToken(fcmToken);
        return userRepository.save(user);
    }
    
    public void deleteUser(String userId) {
        logger.info("Deleting user: {}", userId);
        
        User user = userRepository.findByUserId(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        userRepository.delete(user);
    }
}
