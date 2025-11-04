package com.project.onTrackServer.repository;

import com.project.onTrackServer.model.Order;
import com.project.onTrackServer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderId(String orderId);
    List<Order> findByUser(User user);
    List<Order> findByUserAndIsDeletedFalse(User user);
    Optional<Order> findByIdAndUser(Long id, User user);
    
    // Dashboard Queries
    
    /**
     * Get total count of orders for a user (excluding deleted)
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user = :user AND o.isDeleted = false")
    long countByUserAndNotDeleted(@Param("user") User user);
    
    /**
     * Get total amount spent by a user
     */
    @Query("SELECT COALESCE(SUM(o.price), 0) FROM Order o WHERE o.user = :user AND o.isDeleted = false")
    BigDecimal getTotalSpent(@Param("user") User user);
    
    /**
     * Get total amount spent in last 30 days
     */
    @Query("SELECT COALESCE(SUM(o.price), 0) FROM Order o WHERE o.user = :user " +
           "AND o.isDeleted = false AND o.orderDate >= :thirtyDaysAgo")
    BigDecimal getTotalSpentLast30Days(@Param("user") User user, @Param("thirtyDaysAgo") LocalDateTime thirtyDaysAgo);
    
    /**
     * Count active orders (not DELIVERED and not CANCELLED)
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user = :user " +
           "AND o.isDeleted = false " +
           "AND o.shipmentStatus NOT IN ('DELIVERED', 'CANCELLED')")
    long countActiveOrders(@Param("user") User user);
    
    /**
     * Get monthly spending data for line chart (last 12 months)
     */
    @Query("SELECT DATE_TRUNC('month', o.orderDate) as month, " +
           "COALESCE(SUM(o.price), 0) as totalAmount, " +
           "COUNT(o) as orderCount " +
           "FROM Order o WHERE o.user = :user AND o.isDeleted = false " +
           "AND o.orderDate >= :startDate " +
           "GROUP BY DATE_TRUNC('month', o.orderDate) " +
           "ORDER BY month ASC")
    List<Object[]> getMonthlySpendData(@Param("user") User user, @Param("startDate") LocalDateTime startDate);
    
    /**
     * Get spending by category (for pie chart)
     */
    @Query("SELECT c.categoryName, COALESCE(SUM(o.price), 0) as totalAmount, COUNT(o) as orderCount " +
           "FROM Order o LEFT JOIN o.category c " +
           "WHERE o.user = :user AND o.isDeleted = false " +
           "GROUP BY c.categoryName " +
           "ORDER BY totalAmount DESC")
    List<Object[]> getSpendingByCategory(@Param("user") User user);
    
    /**
     * Get spending by platform (for platform distribution)
     */
    @Query("SELECT p.platformName, COALESCE(SUM(o.price), 0) as totalAmount, COUNT(o) as orderCount " +
           "FROM Order o LEFT JOIN o.platform p " +
           "WHERE o.user = :user AND o.isDeleted = false " +
           "GROUP BY p.platformName " +
           "ORDER BY totalAmount DESC")
    List<Object[]> getSpendingByPlatform(@Param("user") User user);
    
    /**
     * Get active orders (not DELIVERED and not CANCELLED)
     */
    @Query("SELECT o FROM Order o WHERE o.user = :user " +
           "AND o.isDeleted = false " +
           "AND o.shipmentStatus NOT IN ('DELIVERED', 'CANCELLED') " +
           "ORDER BY o.orderDate DESC")
    List<Order> getActiveOrders(@Param("user") User user);
}
