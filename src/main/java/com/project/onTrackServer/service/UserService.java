package com.project.onTrackServer.service;

import com.project.onTrackServer.model.User;
import com.project.onTrackServer.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    public List<User> getAllActiveUsers() {
        return userRepository.findAllActive();
    }
    
    public User saveUser(User user) {
        logger.info("Saving/updating user: {}", user.getEmail());
        
        Optional<User> existingUser = userRepository.findByEmailAndNotDeleted(user.getEmail());
        
        if (existingUser.isPresent()) {
            User userData = existingUser.get();
            userData.setEmail(user.getEmail());
            userData.setName(user.getName());
            userData.setAge(user.getAge());
            if (user.getAuthToken() != null) {
                userData.setAuthToken(user.getAuthToken());
            }
            if (user.getFcmToken() != null) {
                userData.setFcmToken(user.getFcmToken());
            }
            if (user.getPushNotificationEnabled() != null) {
                userData.setPushNotificationEnabled(user.getPushNotificationEnabled());
            }
            if (user.getPollingFrequency() != null) {
                userData.setPollingFrequency(user.getPollingFrequency());
            }
            userData.setUpdateTime(LocalDateTime.now());
            return userRepository.save(userData);
        }
        
        return userRepository.save(user);
    }
    
    public Optional<User> getUserByUserId(Long userId) {
        logger.info("Fetching user: {}", userId);
        return userRepository.findByUserIdAndNotDeleted(userId);
    }
    
    public Optional<User> getUserByEmail(String email) {
        logger.info("Fetching user by email: {}", email);
        return userRepository.findByEmailAndNotDeleted(email);
    }
    
    public List<User> searchUsersByName(String name) {
        logger.info("Searching users by name: {}", name);
        return userRepository.findByNameContainingAndNotDeleted(name);
    }
    
    public List<User> getUsersWithNotificationsEnabled() {
        return userRepository.findUsersWithNotificationsEnabled();
    }
    
    public User updateAuthToken(Long userId, String authToken) {
        logger.info("Updating auth token for user: {}", userId);
        
        User user = userRepository.findByUserIdAndNotDeleted(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        user.setAuthToken(authToken);
        user.setUpdateTime(LocalDateTime.now());
        return userRepository.save(user);
    }
    
    public User updateFcmToken(Long userId, String fcmToken) {
        logger.info("Updating FCM token for user: {}", userId);
        
        User user = userRepository.findByUserIdAndNotDeleted(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        user.setFcmToken(fcmToken);
        user.setUpdateTime(LocalDateTime.now());
        return userRepository.save(user);
    }
    
    public User updateNotificationSettings(Long userId, Boolean pushNotificationEnabled, User.PollingFrequency pollingFrequency) {
        logger.info("Updating notification settings for user: {}", userId);
        
        User user = userRepository.findByUserIdAndNotDeleted(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        if (pushNotificationEnabled != null) {
            user.setPushNotificationEnabled(pushNotificationEnabled);
        }
        if (pollingFrequency != null) {
            user.setPollingFrequency(pollingFrequency);
        }
        user.setUpdateTime(LocalDateTime.now());
        return userRepository.save(user);
    }
    
    public void deleteUser(Long userId, Long deleteUserId) {
        logger.info("Deleting user: {}", userId);
        
        User user = userRepository.findByUserIdAndNotDeleted(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        user.setIsDeleted(true);
        user.setUpdateUser(deleteUserId);
        user.setUpdateTime(LocalDateTime.now());
        userRepository.save(user);
    }
    
    public User createUser(String email, String name, Integer age) {
        logger.info("Creating new user: {}", email);
        
        // Check if user already exists
        Optional<User> existingUser = userRepository.findByEmailAndNotDeleted(email);
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("User with email already exists: " + email);
        }
        
        User user = new User(email, name);
        user.setAge(age);
        return userRepository.save(user);
    }
    
    public User createUserWithDetails(String userId, String email, String name, Integer age, 
                                    String authToken, String fcmToken, Boolean pushNotificationEnabled,
                                    User.PollingFrequency pollingFrequency) {
        logger.info("Creating new user with details: {}", email);
        
        // Check if user already exists
        Optional<User> existingUser = userRepository.findByEmailAndNotDeleted(email);
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("User with email already exists: " + email);
        }
        
        User user = new User(email, name);
        user.setUserId(userId);
        user.setAge(age);
        user.setAuthToken(authToken);
        user.setFcmToken(fcmToken);
        user.setPushNotificationEnabled(pushNotificationEnabled != null ? pushNotificationEnabled : true);
        user.setPollingFrequency(pollingFrequency != null ? pollingFrequency : User.PollingFrequency.THIRTY_MINUTES);
        
        return userRepository.save(user);
    }
}
