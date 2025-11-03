package com.project.onTrackServer.controller;

import com.project.onTrackServer.dto.ApiResponse;
import com.project.onTrackServer.model.Category;
import com.project.onTrackServer.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
@Slf4j
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    @PostMapping("/{userId}")
    public ResponseEntity<Category> createCategory(
            @PathVariable String userId,
            @RequestBody Category category) {
        log.info("Create category request for user: {}", userId);
        Category created = categoryService.createCategory(userId, category);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping("/{userId}/{categoryId}")
    public ResponseEntity<Category> getCategory(
            @PathVariable String userId,
            @PathVariable Long categoryId) {
        log.info("Get category: {} for user: {}", categoryId, userId);
        return categoryService.getCategory(categoryId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<List<Category>> getUserCategories(@PathVariable String userId) {
        log.info("Get all categories for user: {}", userId);
        List<Category> categories = categoryService.getUserCategories(userId);
        return ResponseEntity.ok(categories);
    }
    
    @PutMapping("/{userId}/{categoryId}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable String userId,
            @PathVariable Long categoryId,
            @RequestBody Category category) {
        log.info("Update category: {} for user: {}", categoryId, userId);
        Category updated = categoryService.updateCategory(categoryId, userId, category);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{userId}/{categoryId}")
    public ResponseEntity<ApiResponse> deleteCategory(
            @PathVariable String userId,
            @PathVariable Long categoryId) {
        log.info("Delete category: {} for user: {}", categoryId, userId);
        categoryService.deleteCategory(categoryId, userId);
        return ResponseEntity.ok(new ApiResponse(true, "Category deleted successfully"));
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
