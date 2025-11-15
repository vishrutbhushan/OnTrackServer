package com.project.onTrackServer.service;

import com.project.onTrackServer.model.Platform;
import com.project.onTrackServer.repository.PlatformRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PlatformService extends AbstractCrudService<Platform> {
    
    @Autowired
    private PlatformRepository platformRepository;
    
    @Override
    protected JpaRepository<Platform, Long> getRepository() {
        return platformRepository;
    }
    
    /**
     * Convenience method for backward compatibility.
     * Equivalent to create(userId, platformData).
     */
    public Platform createPlatform(String userId, Platform platformData) {
        return create(userId, platformData);
    }
    
    /**
     * Convenience method for backward compatibility.
     * Equivalent to getResource(platformId, userId).
     */
    public Optional<Platform> getPlatform(Long platformId, String userId) {
        return getResource(platformId, userId);
    }
    
    /**
     * Convenience method for backward compatibility.
     * Equivalent to getUserResources(userId).
     */
    public List<Platform> getUserPlatforms(String userId) {
        return getUserResources(userId);
    }
    
    /**
     * Update platform-specific fields.
     * Uses the generic update mechanism from parent class.
     */
    public Platform updatePlatform(Long platformId, String userId, Platform platformData) {
        log.info("Updating platform: {} for user: {}", platformId, userId);
        
        Platform platform = getPlatform(platformId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Platform not found"));
        
        platform.setPlatformName(platformData.getPlatformName());
        platform.setPlatformRating(platformData.getPlatformRating());
        setUpdateUser(platform, userId);
        
        return platformRepository.save(platform);
    }
    
    /**
     * Convenience method for backward compatibility.
     * Equivalent to delete(platformId, userId).
     */
    public void deletePlatform(Long platformId, String userId) {
        delete(platformId, userId);
    }
}
