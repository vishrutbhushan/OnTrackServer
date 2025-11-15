package com.project.onTrackServer.service;

import com.project.onTrackServer.model.User;
import com.project.onTrackServer.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

/**
 * Abstract base service for user-scoped resources.
 * Provides generic implementations of CRUD operations.
 * 
 * @param <T> The entity type
 */
@Slf4j
public abstract class AbstractCrudService<T> implements CrudService<T> {
    
    @Autowired
    protected UserRepository userRepository;
    
    /**
     * Get the repository for this resource type.
     * Must be implemented by subclasses.
     * 
     * @return The JpaRepository for this resource type
     */
    protected abstract JpaRepository<T, Long> getRepository();
    
    /**
     * Apply entity-specific properties when creating/updating.
     * Default implementation does nothing. Override in subclasses if needed.
     * 
     * @param resourceData The resource data to modify
     * @param userId The user ID (email)
     */
    protected void applyResourceProperties(T resourceData, String userId) {
        // Default implementation - override in subclasses
    }
    
    /**
     * Set the isDeleted flag on the resource.
     * Assumes the entity has a setIsDeleted method.
     * 
     * @param resource The resource to update
     */
    protected void markAsDeleted(T resource) {
        try {
            Method method = resource.getClass().getMethod("setIsDeleted", Boolean.class);
            method.invoke(resource, true);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            log.warn("Could not mark resource as deleted: {}", e.getMessage());
        }
    }
    
    /**
     * Set the updateUser field on the resource.
     * Assumes the entity has a setUpdateUser method.
     * 
     * @param resource The resource to update
     * @param userId The user ID (email)
     */
    protected void setUpdateUser(T resource, String userId) {
        try {
            Method method = resource.getClass().getMethod("setUpdateUser", String.class);
            method.invoke(resource, userId);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            log.warn("Could not set updateUser: {}", e.getMessage());
        }
    }
    
    /**
     * Set the user field on the resource.
     * Assumes the entity has a setUser method.
     * 
     * @param resource The resource to update
     * @param user The user entity
     */
    protected void setUser(T resource, User user) {
        try {
            Method method = resource.getClass().getMethod("setUser", User.class);
            method.invoke(resource, user);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            log.warn("Could not set user: {}", e.getMessage());
        }
    }
    
    /**
     * Set the createUser field on the resource.
     * Assumes the entity has a setCreateUser method.
     * 
     * @param resource The resource to update
     * @param userId The user ID (email)
     */
    protected void setCreateUser(T resource, String userId) {
        try {
            Method method = resource.getClass().getMethod("setCreateUser", String.class);
            method.invoke(resource, userId);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            log.warn("Could not set createUser: {}", e.getMessage());
        }
    }
    
    @Override
    public T create(String userId, T resourceData) {
        log.info("Creating resource for user: {}", userId);
        
        Optional<User> userOpt = userRepository.findByUserId(userId);
        User user = userOpt.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        setUser(resourceData, user);
        setCreateUser(resourceData, userId);
        setUpdateUser(resourceData, userId);
        
        applyResourceProperties(resourceData, userId);
        
        return getRepository().save(resourceData);
    }
    
    @Override
    public Optional<T> getResource(Long resourceId, String userId) {
        log.info("Fetching resource: {} for user: {}", resourceId, userId);
        
        Optional<User> userOpt = userRepository.findByUserId(userId);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        
        return invokeRepositoryMethod("findByIdAndUser", Long.class, User.class, resourceId, userOpt.get());
    }
    
    @Override
    public List<T> getUserResources(String userId) {
        log.info("Fetching all resources for user: {}", userId);
        
        Optional<User> userOpt = userRepository.findByUserId(userId);
        
        return userOpt
                .map(user -> invokeRepositoryMethod("findByUserAndIsDeletedFalse", User.class, user))
                .orElse(List.of());
    }
    
    @Override
    public void delete(Long resourceId, String userId) {
        log.info("Deleting resource: {} for user: {}", resourceId, userId);
        
        T resource = getResource(resourceId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));
        
        markAsDeleted(resource);
        setUpdateUser(resource, userId);
        getRepository().save(resource);
    }
    
    /**
     * Invoke a repository method dynamically with one parameter.
     * 
     * @param methodName The repository method name
     * @param paramType The parameter type class
     * @param param The parameter value
     * @return The method result
     */
    @SuppressWarnings("unchecked")
    private List<T> invokeRepositoryMethod(String methodName, Class<?> paramType, Object param) {
        try {
            Method method = getRepository().getClass().getMethod(methodName, paramType);
            Object result = method.invoke(getRepository(), param);
            return (List<T>) result;
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            log.error("Error invoking repository method {}: {}", methodName, e.getMessage());
            return List.of();
        }
    }
    
    /**
     * Invoke a repository method dynamically with two parameters.
     * 
     * @param methodName The repository method name
     * @param param1Type The first parameter type class
     * @param param2Type The second parameter type class
     * @param param1 The first parameter value
     * @param param2 The second parameter value
     * @return The method result
     */
    @SuppressWarnings("unchecked")
    private Optional<T> invokeRepositoryMethod(String methodName, Class<?> param1Type, Class<?> param2Type, Object param1, Object param2) {
        try {
            Method method = getRepository().getClass().getMethod(methodName, param1Type, param2Type);
            Object result = method.invoke(getRepository(), param1, param2);
            return (Optional<T>) result;
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            log.error("Error invoking repository method {}: {}", methodName, e.getMessage());
            return Optional.empty();
        }
    }
}
