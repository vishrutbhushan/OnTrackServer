package com.project.onTrackServer.service;

import com.project.onTrackServer.model.Category;
import com.project.onTrackServer.model.User;
import com.project.onTrackServer.repository.CategoryRepository;
import com.project.onTrackServer.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * CONTRACT: userId parameter MUST be the email address.
     * This is the only identifier used in API communication.
     */
    public Category createCategory(String userId, Category categoryData) {
        log.info("Creating category for user: {}", userId);
        
        // userId is the email - find user directly
        Optional<User> userOpt = userRepository.findByUserId(userId);
        User user = userOpt.orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        categoryData.setUser(user);
        categoryData.setCreateUser(userId);
        categoryData.setUpdateUser(userId);
        
        return categoryRepository.save(categoryData);
    }

    /**
     * CONTRACT: userId parameter MUST be the email address.
     */
    public Optional<Category> getCategory(Long categoryId, String userId) {
        log.info("Fetching category: {} for user: {}", categoryId, userId);
        
        Optional<User> userOpt = userRepository.findByUserId(userId);
        return userOpt.flatMap(user -> categoryRepository.findByIdAndUser(categoryId, user));
    }

    /**
     * CONTRACT: userId parameter MUST be the email address.
     */
    public List<Category> getUserCategories(String userId) {
        log.info("Fetching all categories for user: {}", userId);
        
        Optional<User> userOpt = userRepository.findByUserId(userId);
        
        return userOpt
                .map(user -> categoryRepository.findByUserAndIsDeletedFalse(user))
                .orElse(List.of());
    }
    
    public Category updateCategory(Long categoryId, String userId, Category categoryData) {
        log.info("Updating category: {} for user: {}", categoryId, userId);
        
        Category category = getCategory(categoryId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        
        category.setCategoryName(categoryData.getCategoryName());
        category.setUpdateUser(userId);
        
        return categoryRepository.save(category);
    }
    
    public void deleteCategory(Long categoryId, String userId) {
        log.info("Deleting category: {} for user: {}", categoryId, userId);
        
        Category category = getCategory(categoryId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        
        category.setIsDeleted(true);
        category.setUpdateUser(userId);
        categoryRepository.save(category);
    }
}
