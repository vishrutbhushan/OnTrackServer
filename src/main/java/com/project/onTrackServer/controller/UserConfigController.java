package com.project.onTrackServer.controller;

import com.project.onTrackServer.dto.ApiResponse;
import com.project.onTrackServer.model.UserConfig;
import com.project.onTrackServer.service.UserConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user-config")
@CrossOrigin(origins = "*")
@Slf4j
public class UserConfigController {
    
    @Autowired
    private UserConfigService userConfigService;
    
    @PostMapping("/{userId}")
    public ResponseEntity<UserConfig> createOrUpdateConfig(
            @PathVariable String userId,
            @RequestBody UserConfig userConfig) {
        log.info("Create/update config request for user: {}", userId);
        UserConfig config = userConfigService.createOrUpdateUserConfig(userId, userConfig);
        return ResponseEntity.ok(config);
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<UserConfig> getConfig(@PathVariable String userId) {
        log.info("Get config request for user: {}", userId);
        return userConfigService.getUserConfig(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{userId}/polling-frequency")
    public ResponseEntity<UserConfig> updatePollingFrequency(
            @PathVariable String userId,
            @RequestBody Map<String, Integer> request) {
        log.info("Update polling frequency for user: {}", userId);
        Integer frequency = request.get("pollingFrequency");
        UserConfig config = userConfigService.updatePollingFrequency(userId, frequency);
        return ResponseEntity.ok(config);
    }
    
    @PutMapping("/{userId}/notification-status")
    public ResponseEntity<UserConfig> updateNotificationStatus(
            @PathVariable String userId,
            @RequestBody Map<String, Boolean> request) {
        log.info("Update notification status for user: {}", userId);
        Boolean enabled = request.get("notificationEnabled");
        UserConfig config = userConfigService.updateNotificationStatus(userId, enabled);
        return ResponseEntity.ok(config);
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
