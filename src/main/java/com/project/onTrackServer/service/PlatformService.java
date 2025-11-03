package com.project.onTrackServer.service;

import com.project.onTrackServer.model.Platform;
import com.project.onTrackServer.model.User;
import com.project.onTrackServer.repository.PlatformRepository;
import com.project.onTrackServer.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PlatformService {
    
    @Autowired
    private PlatformRepository platformRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * CONTRACT: userId parameter MUST be the email address.
     * This is the only identifier used in API communication.
     */
    public Platform createPlatform(String userId, Platform platformData) {
        log.info("Creating platform for user: {}", userId);
        
        // userId is the email - find user directly
        Optional<User> userOpt = userRepository.findByUserId(userId);
        User user = userOpt.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        platformData.setUser(user);
        platformData.setCreateUser(userId);
        platformData.setUpdateUser(userId);
        
        return platformRepository.save(platformData);
    }
    
    /**
     * CONTRACT: userId parameter MUST be the email address.
     */
    public Optional<Platform> getPlatform(Long platformId, String userId) {
        log.info("Fetching platform: {} for user: {}", platformId, userId);
        
        Optional<User> userOpt = userRepository.findByUserId(userId);
        return userOpt.flatMap(user -> platformRepository.findByIdAndUser(platformId, user));
    }
    
    /**
     * CONTRACT: userId parameter MUST be the email address.
     */
    public List<Platform> getUserPlatforms(String userId) {
        log.info("Fetching all platforms for user: {}", userId);
        
        Optional<User> userOpt = userRepository.findByUserId(userId);
        
        return userOpt
                .map(user -> platformRepository.findByUserAndIsDeletedFalse(user))
                .orElse(List.of());
    }
    
    public Platform updatePlatform(Long platformId, String userId, Platform platformData) {
        log.info("Updating platform: {} for user: {}", platformId, userId);
        
        Platform platform = getPlatform(platformId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Platform not found"));
        
        platform.setPlatformName(platformData.getPlatformName());
        platform.setPlatformRating(platformData.getPlatformRating());
        platform.setUpdateUser(userId);
        
        return platformRepository.save(platform);
    }
    
    public void deletePlatform(Long platformId, String userId) {
        log.info("Deleting platform: {} for user: {}", platformId, userId);
        
        Platform platform = getPlatform(platformId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Platform not found"));
        
        platform.setIsDeleted(true);
        platform.setUpdateUser(userId);
        platformRepository.save(platform);
    }
}
