package com.project.onTrackServer.repository;

import com.project.onTrackServer.model.ProductCategories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductCategoriesRepository extends JpaRepository<ProductCategories, Long> {
    
    @Query("SELECT pc FROM ProductCategories pc WHERE pc.productCategories = :categoryName AND pc.isDeleted = false")
    Optional<ProductCategories> findByCategoryName(@Param("categoryName") String categoryName);
    
    @Query("SELECT pc FROM ProductCategories pc WHERE pc.isDeleted = false")
    List<ProductCategories> findAllActive();
    
    @Query("SELECT pc FROM ProductCategories pc WHERE pc.isDeleted = false")
    org.springframework.data.domain.Page<ProductCategories> findAllActive(org.springframework.data.domain.Pageable pageable);
    
    @Query("SELECT pc FROM ProductCategories pc WHERE pc.productCategories LIKE %:categoryName% AND pc.isDeleted = false")
    List<ProductCategories> findByParentCategory(@Param("categoryName") Long parentCategoryId);
    
    @Query("SELECT pc FROM ProductCategories pc WHERE pc.isDeleted = false")
    List<ProductCategories> findRootCategories();
    
    @Query("UPDATE ProductCategories pc SET pc.isDeleted = true WHERE pc.productCategoriesNo = :id")
    void softDeleteById(@Param("id") Long id);
    
    @Query("UPDATE ProductCategories pc SET pc.isDeleted = false WHERE pc.productCategoriesNo = :id")
    void restoreById(@Param("id") Long id);
    
    @Query("SELECT COUNT(pc) FROM ProductCategories pc WHERE pc.isDeleted = false")
    Long countActiveCategories();
    
    @Query("SELECT pc FROM ProductCategories pc WHERE pc.productCategories = :categoryName AND pc.isDeleted = false")
    Optional<ProductCategories> findByCategoryNameAndIsDeletedFalse(@Param("categoryName") String categoryName);
    
    @Query("SELECT pc FROM ProductCategories pc WHERE pc.isDeleted = false")
    List<ProductCategories> findAllByIsDeletedFalse();
    
    @Query("SELECT CASE WHEN COUNT(pc) > 0 THEN true ELSE false END FROM ProductCategories pc WHERE pc.productCategories = :categoryName AND pc.isDeleted = false")
    boolean existsByCategoryNameAndIsDeletedFalse(@Param("categoryName") String categoryName);
    
    @Query("SELECT pc FROM ProductCategories pc WHERE pc.productCategories LIKE %:keyword% AND pc.isDeleted = false")
    List<ProductCategories> findByCategoryNameContainingIgnoreCaseAndIsDeletedFalse(@Param("keyword") String keyword);
}
