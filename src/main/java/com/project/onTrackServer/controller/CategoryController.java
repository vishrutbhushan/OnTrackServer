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
public class CategoryController implements CrudController<Category> {
    
    @Autowired
    private CategoryService categoryService;
    
    @Override
    @PostMapping("/{userId}")
    public ResponseEntity<Category> create(
            @PathVariable String userId,
            @RequestBody Category category) {
        log.info("Create category request for user: {}", userId);
        Category created = categoryService.createCategory(userId, category);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @Override
    @GetMapping("/{userId}")
    public ResponseEntity<List<Category>> getUserResources(@PathVariable String userId) {
        log.info("Get all categories for user: {}", userId);
        List<Category> categories = categoryService.getUserCategories(userId);
        return ResponseEntity.ok(categories);
    }
    
    @Override
    @DeleteMapping("/{userId}/{categoryId}")
    public ResponseEntity<ApiResponse> delete(
            @PathVariable String userId,
            @PathVariable Long categoryId) {
        log.info("Delete category: {} for user: {}", categoryId, userId);
        categoryService.deleteCategory(categoryId, userId);
        return ResponseEntity.ok(new ApiResponse(true, "Category deleted successfully"));
    }

}
