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
public class PlatformController implements CrudController<Platform> {
    
    @Autowired
    private PlatformService platformService;
    
    @Override
    @PostMapping("/{userId}")
    public ResponseEntity<Platform> create(
            @PathVariable String userId,
            @RequestBody Platform platform) {
        log.info("Create platform request for user: {}", userId);
        Platform created = platformService.createPlatform(userId, platform);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @Override
    @GetMapping("/{userId}")
    public ResponseEntity<List<Platform>> getUserResources(@PathVariable String userId) {
        log.info("Get all platforms for user: {}", userId);
        List<Platform> platforms = platformService.getUserPlatforms(userId);
        return ResponseEntity.ok(platforms);
    }

    @Override
    @DeleteMapping("/{userId}/{platformId}")
    public ResponseEntity<ApiResponse> delete(
            @PathVariable String userId,
            @PathVariable Long platformId) {
        log.info("Delete platform: {} for user: {}", platformId, userId);
        platformService.deletePlatform(platformId, userId);
        return ResponseEntity.ok(new ApiResponse(true, "Platform deleted successfully"));
    }
}
