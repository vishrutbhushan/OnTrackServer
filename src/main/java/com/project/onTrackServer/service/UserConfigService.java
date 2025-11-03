package com.project.onTrackServer.service;

import com.project.onTrackServer.model.UserConfig;
import com.project.onTrackServer.model.User;
import com.project.onTrackServer.repository.UserConfigRepository;
import com.project.onTrackServer.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class UserConfigService {
    
    @Autowired
    private UserConfigRepository userConfigRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * CONTRACT: userId parameter MUST be the email address.
     * This is the only identifier used in API communication.
     */
    public UserConfig createOrUpdateUserConfig(String userId, UserConfig userConfigData) {
        log.info("Creating/updating user config for user: {}", userId);
        
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        Optional<UserConfig> existingConfig = userConfigRepository.findByUser(user);
        
        UserConfig userConfig;
        if (existingConfig.isPresent()) {
            userConfig = existingConfig.get();
            userConfig.setPollingFrequency(userConfigData.getPollingFrequency());
            userConfig.setNotificationEnabled(userConfigData.getNotificationEnabled());
            userConfig.setUpdateUser(userId);
        } else {
            userConfig = new UserConfig();
            userConfig.setUser(user);
            userConfig.setPollingFrequency(userConfigData.getPollingFrequency());
            userConfig.setNotificationEnabled(userConfigData.getNotificationEnabled());
            userConfig.setCreateUser(userId);
            userConfig.setUpdateUser(userId);
            
            // Set the bidirectional relationship
            user.setUserConfig(userConfig);
        }
        
        // Save through userConfigRepository (cascade will handle saving user if needed)
        return userConfigRepository.save(userConfig);
    }
    
    /**
     * CONTRACT: userId parameter MUST be the email address.
     */
    public Optional<UserConfig> getUserConfig(String userId) {
        log.info("Fetching user config for user: {}", userId);
        
        return userRepository.findByUserId(userId)
                .flatMap(userConfigRepository::findByUser);
    }
    
    public UserConfig updatePollingFrequency(String userId, Integer frequency) {
        log.info("Updating polling frequency for user: {}", userId);
        
        UserConfig userConfig = getUserConfig(userId)
                .orElseThrow(() -> new IllegalArgumentException("User config not found for user: " + userId));
        
        userConfig.setPollingFrequency(frequency);
        userConfig.setUpdateUser(userId);
        return userConfigRepository.save(userConfig);
    }
    
    public UserConfig updateNotificationStatus(String userId, Boolean enabled) {
        log.info("Updating notification status for user: {}", userId);
        
        UserConfig userConfig = getUserConfig(userId)
                .orElseThrow(() -> new IllegalArgumentException("User config not found for user: " + userId));
        
        userConfig.setNotificationEnabled(enabled);
        userConfig.setUpdateUser(userId);
        return userConfigRepository.save(userConfig);
    }
}
