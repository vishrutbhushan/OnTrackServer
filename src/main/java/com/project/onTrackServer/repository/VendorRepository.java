package com.project.onTrackServer.repository;

import com.project.onTrackServer.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {
    
    Optional<Vendor> findByNameAndIsDeletedFalse(String name);
    
    List<Vendor> findAllByIsDeletedFalse();
    
    @Query("SELECT v FROM Vendor v WHERE v.canDelay <= :maxDelay AND v.isDeleted = false")
    List<Vendor> findByMaxDelayRisk(@Param("maxDelay") Integer maxDelay);
    
    @Query("SELECT v FROM Vendor v WHERE v.canBeBadQuality <= :maxQualityRisk AND v.isDeleted = false")
    List<Vendor> findByMaxQualityRisk(@Param("maxQualityRisk") Integer maxQualityRisk);
    
    @Query("SELECT v FROM Vendor v WHERE v.avgRating >= :minRating AND v.isDeleted = false")
    List<Vendor> findByMinRating(@Param("minRating") BigDecimal minRating);
    
    boolean existsByNameAndIsDeletedFalse(String name);
    
    @Query("SELECT COUNT(v) FROM Vendor v WHERE v.isDeleted = false")
    long countActiveVendors();
    
    // Additional methods for VendorController
    @Query("SELECT v FROM Vendor v WHERE v.vendorId = :vendorId AND v.isDeleted = false")
    Optional<Vendor> findByVendorIdAndIsDeletedFalse(@Param("vendorId") Long vendorId);
    
    @Query("SELECT v FROM Vendor v WHERE v.isDeleted = false")
    List<Vendor> findByIsDeletedFalse();
    
    @Query("SELECT v FROM Vendor v WHERE v.isDeleted = false")
    org.springframework.data.domain.Page<Vendor> findByIsDeletedFalse(org.springframework.data.domain.Pageable pageable);
    
    @Query("SELECT v FROM Vendor v WHERE v.canDelay <= :canDelay AND v.canBeBadQuality <= :canBeBadQuality AND v.isDeleted = false")
    List<Vendor> findByCanDelayLessThanEqualAndCanBeBadQualityLessThanEqualAndIsDeletedFalse(
        @Param("canDelay") int canDelay, 
        @Param("canBeBadQuality") int canBeBadQuality
    );
    
    @Query("SELECT v FROM Vendor v WHERE v.avgRating >= :minRating AND v.isDeleted = false")
    List<Vendor> findByAvgRatingGreaterThanEqualAndIsDeletedFalse(@Param("minRating") int minRating);
    
    @Query("SELECT COUNT(v) FROM Vendor v WHERE v.isDeleted = false")
    Long countByIsDeletedFalse();
}
