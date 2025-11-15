package com.project.onTrackServer.controller;

import com.project.onTrackServer.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Generic interface for user-scoped resource controllers.
 * Provides standard CRUD operations for entities that belong to a specific user.
 * 
 * @param <T> The entity type (e.g., Category, Platform)
 */
public interface CrudController<T> {
    
    /**
     * Create a new resource for the given user.
     * 
     * @param userId The user ID (email)
     * @param resource The resource data
     * @return The created resource
     */
    @PostMapping("/{userId}")
    ResponseEntity<T> create(
            @PathVariable String userId,
            @RequestBody T resource);
    
    /**
     * Get all resources for the given user.
     * 
     * @param userId The user ID (email)
     * @return List of resources for the user
     */
    @GetMapping("/{userId}")
    ResponseEntity<List<T>> getUserResources(@PathVariable String userId);
    
    /**
     * Delete a resource for the given user.
     * 
     * @param userId The user ID (email)
     * @param resourceId The resource ID
     * @return API response confirming deletion
     */
    @DeleteMapping("/{userId}/{resourceId}")
    ResponseEntity<ApiResponse> delete(
            @PathVariable String userId,
            @PathVariable Long resourceId);
}
