package com.project.onTrackServer.controller;

import com.project.onTrackServer.dto.ApiResponse;
import com.project.onTrackServer.model.Platform;
import com.project.onTrackServer.service.PlatformService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/platforms")
@CrossOrigin(origins = "*")
@Slf4j
public class PlatformController {
    
    @Autowired
    private PlatformService platformService;
    
    @PostMapping("/{userId}")
    public ResponseEntity<Platform> createPlatform(
            @PathVariable String userId,
            @RequestBody Platform platform) {
        log.info("Create platform request for user: {}", userId);
        Platform created = platformService.createPlatform(userId, platform);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping("/{userId}/{platformId}")
    public ResponseEntity<Platform> getPlatform(
            @PathVariable String userId,
            @PathVariable Long platformId) {
        log.info("Get platform: {} for user: {}", platformId, userId);
        return platformService.getPlatform(platformId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<List<Platform>> getUserPlatforms(@PathVariable String userId) {
        log.info("Get all platforms for user: {}", userId);
        List<Platform> platforms = platformService.getUserPlatforms(userId);
        return ResponseEntity.ok(platforms);
    }
    
    @PutMapping("/{userId}/{platformId}")
    public ResponseEntity<Platform> updatePlatform(
            @PathVariable String userId,
            @PathVariable Long platformId,
            @RequestBody Platform platform) {
        log.info("Update platform: {} for user: {}", platformId, userId);
        Platform updated = platformService.updatePlatform(platformId, userId, platform);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{userId}/{platformId}")
    public ResponseEntity<ApiResponse> deletePlatform(
            @PathVariable String userId,
            @PathVariable Long platformId) {
        log.info("Delete platform: {} for user: {}", platformId, userId);
        platformService.deletePlatform(platformId, userId);
        return ResponseEntity.ok(new ApiResponse(true, "Platform deleted successfully"));
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.error("Error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(false, e.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(Exception e) {
        log.error("Unexpected error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Internal server error: " + e.getMessage()));
    }
}
