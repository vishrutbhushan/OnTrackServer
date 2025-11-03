package com.project.onTrackServer.controller;

import com.project.onTrackServer.dto.ApiResponse;
import com.project.onTrackServer.model.LogisticProvider;
import com.project.onTrackServer.service.LogisticProviderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/logistic-providers")
@CrossOrigin(origins = "*")
@Slf4j
public class LogisticProviderController {
    
    @Autowired
    private LogisticProviderService logisticProviderService;
    
    @PostMapping
    public ResponseEntity<LogisticProvider> createProvider(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody LogisticProvider provider) {
        log.info("Create logistic provider request");
        LogisticProvider created = logisticProviderService.createProvider(userId, provider);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping("/{providerId}")
    public ResponseEntity<LogisticProvider> getProvider(@PathVariable Long providerId) {
        log.info("Get logistic provider: {}", providerId);
        return logisticProviderService.getProvider(providerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/name/{providerName}")
    public ResponseEntity<LogisticProvider> getProviderByName(@PathVariable String providerName) {
        log.info("Get logistic provider by name: {}", providerName);
        return logisticProviderService.getProviderByName(providerName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<LogisticProvider>> getAllProviders() {
        log.info("Get all logistic providers");
        List<LogisticProvider> providers = logisticProviderService.getAllProviders();
        return ResponseEntity.ok(providers);
    }
    
    @PutMapping("/{providerId}")
    public ResponseEntity<LogisticProvider> updateProvider(
            @PathVariable Long providerId,
            @RequestHeader("X-User-Id") String userId,
            @RequestBody LogisticProvider provider) {
        log.info("Update logistic provider: {}", providerId);
        LogisticProvider updated = logisticProviderService.updateProvider(providerId, userId, provider);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{providerId}")
    public ResponseEntity<ApiResponse> deleteProvider(
            @PathVariable Long providerId,
            @RequestHeader("X-User-Id") String userId) {
        log.info("Delete logistic provider: {}", providerId);
        logisticProviderService.deleteProvider(providerId, userId);
        return ResponseEntity.ok(new ApiResponse(true, "Logistic provider deleted successfully"));
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
