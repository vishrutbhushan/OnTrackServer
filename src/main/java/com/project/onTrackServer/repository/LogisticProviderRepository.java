package com.project.onTrackServer.repository;

import com.project.onTrackServer.model.LogisticProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface LogisticProviderRepository extends JpaRepository<LogisticProvider, Long> {
    
    Optional<LogisticProvider> findByNameAndIsDeletedFalse(String name);
    
    List<LogisticProvider> findAllByIsDeletedFalse();
    
    @Query("SELECT l FROM LogisticProvider l WHERE l.avgRating >= :minRating AND l.isDeleted = false")
    List<LogisticProvider> findByMinRating(@Param("minRating") BigDecimal minRating);
    
    @Query("SELECT l FROM LogisticProvider l WHERE l.canDelay <= :maxDelay AND l.isDeleted = false")
    List<LogisticProvider> findByMaxDelayRisk(@Param("maxDelay") Integer maxDelay);
    
    boolean existsByNameAndIsDeletedFalse(String name);
    
    // Additional methods for LogisticProviderService
    @Query("SELECT l FROM LogisticProvider l WHERE l.name = :name AND l.isDeleted = false")
    Optional<LogisticProvider> findByName(@Param("name") String name);
    
    @Query("SELECT l FROM LogisticProvider l WHERE l.isDeleted = false")
    List<LogisticProvider> findAllActive();
    
    @Query("SELECT l FROM LogisticProvider l WHERE l.isDeleted = false")
    org.springframework.data.domain.Page<LogisticProvider> findAllActive(org.springframework.data.domain.Pageable pageable);
    
    @Query("SELECT l FROM LogisticProvider l WHERE l.avgRating >= :minRating AND l.isDeleted = false")
    List<LogisticProvider> findByMinimumRating(@Param("minRating") double minRating);
    
    @Query("SELECT l FROM LogisticProvider l WHERE l.canDelay <= 3 AND l.canBeBadQuality <= 3 AND l.isDeleted = false")
    List<LogisticProvider> findReliableProviders();
    
    @Query("SELECT l FROM LogisticProvider l WHERE l.canDelay <= 2 AND l.isDeleted = false")
    List<LogisticProvider> findLowRiskProviders();
    
    @Query("SELECT l FROM LogisticProvider l WHERE l.canBeBadQuality <= 2 AND l.isDeleted = false")
    List<LogisticProvider> findHighQualityProviders();
    
    @Query("UPDATE LogisticProvider l SET l.isDeleted = true WHERE l.logisticId = :id")
    void softDeleteById(@Param("id") Long id);
    
    @Query("UPDATE LogisticProvider l SET l.isDeleted = false WHERE l.logisticId = :id")
    void restoreById(@Param("id") Long id);
    
    @Query("SELECT COUNT(l) FROM LogisticProvider l WHERE l.isDeleted = false")
    Long countActiveProviders();
}
