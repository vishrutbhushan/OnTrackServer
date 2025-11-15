package com.project.onTrackServer.service;

import java.util.List;
import java.util.Optional;

/**
 * Generic interface for user-scoped resource services.
 * Provides standard CRUD operations for entities that belong to a specific user.
 * 
 * @param <T> The entity type (e.g., Category, Platform)
 */
public interface CrudService<T> {
    
    /**
     * Create a new resource for the given user.
     * CONTRACT: userId parameter MUST be the email address.
     * 
     * @param userId The user ID (email)
     * @param resourceData The resource data
     * @return The created resource
     */
    T create(String userId, T resourceData);
    
    /**
     * Get a specific resource by ID and userId.
     * CONTRACT: userId parameter MUST be the email address.
     * 
     * @param resourceId The resource ID
     * @param userId The user ID (email)
     * @return Optional containing the resource if found
     */
    Optional<T> getResource(Long resourceId, String userId);
    
    /**
     * Get all resources for the given user.
     * CONTRACT: userId parameter MUST be the email address.
     * 
     * @param userId The user ID (email)
     * @return List of resources for the user
     */
    List<T> getUserResources(String userId);
    
    /**
     * Delete a resource for the given user (soft delete).
     * 
     * @param resourceId The resource ID
     * @param userId The user ID (email)
     */
    void delete(Long resourceId, String userId);
}
