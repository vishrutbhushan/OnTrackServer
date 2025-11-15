package com.project.onTrackServer.service;

import com.project.onTrackServer.model.Category;
import com.project.onTrackServer.repository.CategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CategoryService extends AbstractCrudService<Category> {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Override
    protected JpaRepository<Category, Long> getRepository() {
        return categoryRepository;
    }
    
    /**
     * Convenience method for backward compatibility.
     * Equivalent to create(userId, categoryData).
     */
    public Category createCategory(String userId, Category categoryData) {
        return create(userId, categoryData);
    }

    /**
     * Convenience method for backward compatibility.
     * Equivalent to getResource(categoryId, userId).
     */
    public Optional<Category> getCategory(Long categoryId, String userId) {
        return getResource(categoryId, userId);
    }

    /**
     * Convenience method for backward compatibility.
     * Equivalent to getUserResources(userId).
     */
    public List<Category> getUserCategories(String userId) {
        return getUserResources(userId);
    }

    /**
     * Convenience method for backward compatibility.
     * Equivalent to delete(categoryId, userId).
     */
    public void deleteCategory(Long categoryId, String userId) {
        delete(categoryId, userId);
    }
}
