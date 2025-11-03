package com.project.onTrackServer.service;

import com.project.onTrackServer.model.User;
import com.project.onTrackServer.model.UserConfig;
import com.project.onTrackServer.dto.UserDTO;
import com.project.onTrackServer.repository.UserRepository;
import com.project.onTrackServer.repository.UserConfigRepository;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private UserConfigRepository userConfigRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public UserDTO saveUser(UserDTO userDTO) {
        // CONTRACT: userId ALWAYS contains the email address
        // The numeric 'id' field is NEVER used in API communication
        String email = userDTO.getUserId(); // userId must be email
        logger.info("Saving/updating user with email: {}", email);

        // Find existing user by userId (which contains email)
        Optional<User> existingUser = userRepository.findByUserId(email);
        
        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
            logger.info("Updating existing user: {}", email);
            user.setEmail(email);
            user.setDisplayName(userDTO.getDisplayName());
            if (userDTO.getAccessToken() != null) {
                user.setAccessToken(userDTO.getAccessToken());
            }
            if (userDTO.getFcmToken() != null) {
                user.setFcmToken(userDTO.getFcmToken());
            }
        } else {
            logger.info("Creating new user with email: {}", email);
            user = new User();
            user.setUserId(email);  // CRITICAL: userId = email
            user.setEmail(email);
            user.setDisplayName(userDTO.getDisplayName());
            user.setAccessToken(userDTO.getAccessToken());
            user.setFcmToken(userDTO.getFcmToken());
        }

        user = userRepository.save(user);
        logger.info("User persisted successfully with email: {}", email);
        
        // Save or update UserConfig
        saveUserConfig(user, userDTO);
        
        return new UserDTO(user);
    }
    
    private void saveUserConfig(User user, UserDTO userDTO) {
        UserConfig userConfig = userConfigRepository.findByUser(user)
                .orElse(new UserConfig());
        
        userConfig.setUser(user);
        
        if (userDTO.getPollingFrequency() != null) {
            userConfig.setPollingFrequency(userDTO.getPollingFrequency());
        } else if (userConfig.getPollingFrequency() == null) {
            userConfig.setPollingFrequency(10);
        }
        
        if (userDTO.getNotificationEnabled() != null) {
            userConfig.setNotificationEnabled(userDTO.getNotificationEnabled());
        } else if (userConfig.getNotificationEnabled() == null) {
            userConfig.setNotificationEnabled(true);
        }
        
        if (userDTO.getAutoArchiveOrderEmails() != null) {
            userConfig.setAutoArchiveOrderEmails(userDTO.getAutoArchiveOrderEmails());
        } else if (userConfig.getAutoArchiveOrderEmails() == null) {
            userConfig.setAutoArchiveOrderEmails(true);
        }
        
        userConfigRepository.save(userConfig);
    }

    public Optional<UserDTO> getUserByUserId(String userId) {
        logger.info("Fetching user: {}", userId);
        return userRepository.findByUserId(userId).map(UserDTO::new);
    }

    public Optional<User> getUserByEmail(String email) {
        logger.info("Fetching user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    public UserDTO updateAccessToken(String userId, String accessToken) {
        logger.info("Updating access token for user: {}", userId);

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setAccessToken(accessToken);
        user = userRepository.save(user);
        return new UserDTO(user);
    }

    public UserDTO updateFcmToken(String userId, String fcmToken) {
        logger.info("Updating FCM token for user: {}", userId);

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        user.setFcmToken(fcmToken);
        user = userRepository.save(user);
        return new UserDTO(user);
    }
    
    public UserDTO updateUserConfig(String userId, UserDTO configUpdates) {
        logger.info("Updating user config for: {}", userId);
        
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        saveUserConfig(user, configUpdates);
        user = userRepository.save(user);
        return new UserDTO(user);
    }

    public void deleteUser(String userId) {
        logger.info("Deleting user: {}", userId);

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        userRepository.delete(user);
    }
}
