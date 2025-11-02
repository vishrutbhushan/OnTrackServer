package com.project.onTrackServer.service;

import com.project.onTrackServer.model.ProductCategories;
import com.project.onTrackServer.repository.ProductCategoriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductCategoriesService {

    private final ProductCategoriesRepository productCategoriesRepository;

    @Autowired
    public ProductCategoriesService(ProductCategoriesRepository productCategoriesRepository) {
        this.productCategoriesRepository = productCategoriesRepository;
    }

    public ProductCategories save(ProductCategories category) {
        if (category.getProductCategoriesNo() == null) {
            category.setCreateTime(LocalDateTime.now());
        }
        category.setUpdateTime(LocalDateTime.now());
        return productCategoriesRepository.save(category);
    }

    public Optional<ProductCategories> findById(Long id) {
        return productCategoriesRepository.findById(id);
    }

    public Optional<ProductCategories> findByCategoryName(String categoryName) {
        return productCategoriesRepository.findByCategoryName(categoryName);
    }

    @Transactional(readOnly = true)
    public List<ProductCategories> findAllActive() {
        return productCategoriesRepository.findAllActive();
    }

    @Transactional(readOnly = true)
    public Page<ProductCategories> findAllActive(Pageable pageable) {
        return productCategoriesRepository.findAllActive(pageable);
    }

    @Transactional(readOnly = true)
    public List<ProductCategories> findByParentCategory(Long parentId) {
        return productCategoriesRepository.findByParentCategory(parentId);
    }

    @Transactional(readOnly = true)
    public List<ProductCategories> findRootCategories() {
        return productCategoriesRepository.findRootCategories();
    }

    public void softDelete(Long id) {
        productCategoriesRepository.softDeleteById(id);
    }

    public void restore(Long id) {
        productCategoriesRepository.restoreById(id);
    }

    public ProductCategories update(Long id, ProductCategories updatedCategory) {
        return findById(id)
                .map(category -> {
                    category.setProductCategories(updatedCategory.getProductCategories());
                    category.setUpdateTime(LocalDateTime.now());
                    return save(category);
                })
                .orElseThrow(() -> new RuntimeException("Product category not found with id: " + id));
    }

    public void deleteById(Long id) {
        productCategoriesRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByCategoryName(String categoryName) {
        return productCategoriesRepository.findByCategoryName(categoryName).isPresent();
    }

    @Transactional(readOnly = true)
    public long countActiveCategories() {
        return productCategoriesRepository.countActiveCategories();
    }

    public ProductCategories createRootCategory(String categoryName) {
        ProductCategories category = new ProductCategories();
        category.setProductCategories(categoryName);
        return save(category);
    }

    public ProductCategories createSubCategory(String categoryName, Long parentId) {
        // Note: The new schema doesn't have parent-child relationships
        // This method now just creates a category with the given name
        ProductCategories category = new ProductCategories();
        category.setProductCategories(categoryName);
        return save(category);
    }
}
