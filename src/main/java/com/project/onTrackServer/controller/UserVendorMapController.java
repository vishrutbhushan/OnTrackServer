package com.project.onTrackServer.controller;

import com.project.onTrackServer.model.ApiResponse;
import com.project.onTrackServer.model.UserVendorMap;
import com.project.onTrackServer.service.UserVendorMapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user-vendor-map")
@CrossOrigin(origins = "*")
public class UserVendorMapController {
    
    @Autowired
    private UserVendorMapService userVendorMapService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserVendorMap>>> getAllActiveUserVendorMaps() {
        try {
            List<UserVendorMap> userVendorMaps = userVendorMapService.getAllActiveUserVendorMaps();
            return ResponseEntity.ok(new ApiResponse<>(true, "User-Vendor mappings retrieved successfully", userVendorMaps));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error retrieving user-vendor mappings: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<UserVendorMap>>> getVendorsByUser(@PathVariable Long userId) {
        try {
            List<UserVendorMap> userVendorMaps = userVendorMapService.getVendorsByUser(userId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Vendors for user retrieved successfully", userVendorMaps));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error retrieving vendors for user: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<ApiResponse<List<UserVendorMap>>> getUsersByVendor(@PathVariable Long vendorId) {
        try {
            List<UserVendorMap> userVendorMaps = userVendorMapService.getUsersByVendor(vendorId);
            return ResponseEntity.ok(new ApiResponse<>(true, "Users for vendor retrieved successfully", userVendorMaps));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error retrieving users for vendor: " + e.getMessage(), null));
        }
    }
    
    @GetMapping("/user/{userId}/vendor/{vendorId}")
    public ResponseEntity<ApiResponse<UserVendorMap>> getUserVendorMapping(
            @PathVariable Long userId, 
            @PathVariable Long vendorId) {
        try {
            Optional<UserVendorMap> userVendorMap = userVendorMapService.findByUserAndVendor(userId, vendorId);
            if (userVendorMap.isPresent()) {
                return ResponseEntity.ok(new ApiResponse<>(true, "User-Vendor mapping found", userVendorMap.get()));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "User-Vendor mapping not found", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error retrieving user-vendor mapping: " + e.getMessage(), null));
        }
    }
    
    @PostMapping
    public ResponseEntity<ApiResponse<UserVendorMap>> createUserVendorMapping(
            @RequestBody CreateUserVendorMappingRequest request) {
        try {
            UserVendorMap createdMapping = userVendorMapService.createUserVendorMapping(
                    request.getUserId(), 
                    request.getVendorId(), 
                    request.getCreateUserId()
            );
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "User-Vendor mapping created successfully", createdMapping));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error creating user-vendor mapping: " + e.getMessage(), null));
        }
    }
    
    @PutMapping("/{userVendorId}")
    public ResponseEntity<ApiResponse<UserVendorMap>> updateUserVendorMapping(
            @PathVariable Long userVendorId,
            @RequestBody UpdateUserVendorMappingRequest request) {
        try {
            UserVendorMap updatedMapping = userVendorMapService.updateUserVendorMapping(
                    userVendorId, 
                    request.getUpdateUserId()
            );
            return ResponseEntity.ok(new ApiResponse<>(true, "User-Vendor mapping updated successfully", updatedMapping));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error updating user-vendor mapping: " + e.getMessage(), null));
        }
    }
    
    @DeleteMapping("/{userVendorId}")
    public ResponseEntity<ApiResponse<Void>> deleteUserVendorMapping(
            @PathVariable Long userVendorId,
            @RequestBody DeleteUserVendorMappingRequest request) {
        try {
            userVendorMapService.deleteUserVendorMapping(userVendorId, request.getDeleteUserId());
            return ResponseEntity.ok(new ApiResponse<>(true, "User-Vendor mapping deleted successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error deleting user-vendor mapping: " + e.getMessage(), null));
        }
    }
    
    @DeleteMapping("/user/{userId}/vendor/{vendorId}")
    public ResponseEntity<ApiResponse<Void>> deleteUserVendorMappingByUserAndVendor(
            @PathVariable Long userId,
            @PathVariable Long vendorId,
            @RequestBody DeleteUserVendorMappingRequest request) {
        try {
            userVendorMapService.deleteByUserAndVendor(userId, vendorId, request.getDeleteUserId());
            return ResponseEntity.ok(new ApiResponse<>(true, "User-Vendor mapping deleted successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Error deleting user-vendor mapping: " + e.getMessage(), null));
        }
    }
    
    // Request DTOs
    public static class CreateUserVendorMappingRequest {
        private Long userId;
        private Long vendorId;
        private Long createUserId;
        
        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public Long getVendorId() { return vendorId; }
        public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
        
        public Long getCreateUserId() { return createUserId; }
        public void setCreateUserId(Long createUserId) { this.createUserId = createUserId; }
    }
    
    public static class UpdateUserVendorMappingRequest {
        private Long updateUserId;
        
        // Getters and Setters
        public Long getUpdateUserId() { return updateUserId; }
        public void setUpdateUserId(Long updateUserId) { this.updateUserId = updateUserId; }
    }
    
    public static class DeleteUserVendorMappingRequest {
        private Long deleteUserId;
        
        // Getters and Setters
        public Long getDeleteUserId() { return deleteUserId; }
        public void setDeleteUserId(Long deleteUserId) { this.deleteUserId = deleteUserId; }
    }
}
