package com.project.onTrackServer.repository;

import com.project.onTrackServer.model.EcommercePlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface EcommercePlatformRepository extends JpaRepository<EcommercePlatform, Long> {
    
    Optional<EcommercePlatform> findByNameAndIsDeletedFalse(String name);
    
    List<EcommercePlatform> findAllByIsDeletedFalse();
    
    @Query("SELECT e FROM EcommercePlatform e WHERE e.avgRating >= :minRating AND e.isDeleted = false")
    List<EcommercePlatform> findByMinRating(@Param("minRating") BigDecimal minRating);
    
    boolean existsByNameAndIsDeletedFalse(String name);
    
    @Query("SELECT COUNT(e) FROM EcommercePlatform e WHERE e.isDeleted = false")
    long countActiveEcommercePlatforms();
}
