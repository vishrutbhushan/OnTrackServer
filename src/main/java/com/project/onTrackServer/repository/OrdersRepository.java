package com.project.onTrackServer.repository;

import com.project.onTrackServer.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdersRepository extends JpaRepository<Orders, Long> {
    
    List<Orders> findByUserIdAndIsDeletedFalse(Long userId);
    
    Optional<Orders> findByOrderNoAndIsDeletedFalse(String orderNo);
    
    List<Orders> findByCurrentStatusAndIsDeletedFalse(String currentStatus);
    
    @Query("SELECT o FROM Orders o WHERE o.user.id = :userId AND o.createTime BETWEEN :startDate AND :endDate AND o.isDeleted = false")
    List<Orders> findOrdersByUserAndDateRange(@Param("userId") Long userId, 
                                            @Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT o FROM Orders o WHERE o.vendor.vendorId = :vendorId AND o.isDeleted = false")
    List<Orders> findByVendorId(@Param("vendorId") Long vendorId);
    
    @Query("SELECT o FROM Orders o WHERE o.ecommercePlatform.platformId = :platformId AND o.isDeleted = false")
    List<Orders> findByEcommercePlatformId(@Param("platformId") Long platformId);
    
    @Query("SELECT COUNT(o) FROM Orders o WHERE o.user.id = :userId AND o.isDeleted = false")
    long countOrdersByUser(@Param("userId") Long userId);
    
    boolean existsByOrderNoAndIsDeletedFalse(String orderNo);
    
    @Query("SELECT o FROM Orders o WHERE o.delayPossible >= :riskLevel AND o.isDeleted = false")
    List<Orders> findOrdersWithDelayRisk(@Param("riskLevel") Integer riskLevel);
    
    @Query("SELECT o FROM Orders o WHERE o.badQualityPossible >= :riskLevel AND o.isDeleted = false")
    List<Orders> findOrdersWithQualityRisk(@Param("riskLevel") Integer riskLevel);
}
