package com.project.onTrackServer.service;

import com.project.onTrackServer.dto.DashboardStatsDTO;
import com.project.onTrackServer.model.Order;
import com.project.onTrackServer.model.User;
import com.project.onTrackServer.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Dashboard Statistics and Analytics
 */
@Service
public class DashboardService {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);
    
    @Autowired
    private OrderRepository orderRepository;
    
    /**
     * Get comprehensive dashboard statistics for a user
     */
    public DashboardStatsDTO getDashboardStats(User user) {
        try {
            logger.info("Generating dashboard stats for user: {}", user.getUserId());
            
            DashboardStatsDTO stats = new DashboardStatsDTO();
            
            // 1. Total Orders
            stats.setTotalOrders(orderRepository.countByUserAndNotDeleted(user));
            logger.debug("Total orders: {}", stats.getTotalOrders());
            
            // 2. Total Spent
            stats.setTotalSpent(orderRepository.getTotalSpent(user));
            logger.debug("Total spent: {}", stats.getTotalSpent());
            
            // 3. Last 30 Days Spent
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            stats.setLast30DaysSpent(orderRepository.getTotalSpentLast30Days(user, thirtyDaysAgo));
            logger.debug("Last 30 days spent: {}", stats.getLast30DaysSpent());
            
            // 4. Active Orders
            stats.setActiveOrders(orderRepository.countActiveOrders(user));
            logger.debug("Active orders: {}", stats.getActiveOrders());
            
            // 5. Monthly Spending Data (Line Chart)
            stats.setMonthlySpendData(getMonthlySpendData(user));
            logger.debug("Monthly spend data points: {}", stats.getMonthlySpendData().size());
            
            // 6. Category Spending Data (Pie Chart)
            stats.setCategorySpendData(getCategorySpendData(user, stats.getTotalSpent()));
            logger.debug("Category data points: {}", stats.getCategorySpendData().size());
            
            // 7. Platform Distribution
            stats.setPlatformDistribution(getPlatformDistribution(user, stats.getTotalSpent()));
            logger.debug("Platform data points: {}", stats.getPlatformDistribution().size());
            
            // 8. Active Orders List
            stats.setActiveOrdersList(getActiveOrdersList(user));
            logger.debug("Active orders list size: {}", stats.getActiveOrdersList().size());
            
            logger.info("Dashboard stats generated successfully for user: {}", user.getUserId());
            return stats;
            
        } catch (Exception e) {
            logger.error("Error generating dashboard stats for user {}: {}", user.getUserId(), e.getMessage(), e);
            throw new RuntimeException("Failed to generate dashboard statistics", e);
        }
    }
    
    /**
     * Get monthly spending data for line chart (last 12 months)
     */
    private List<DashboardStatsDTO.MonthlySpendDTO> getMonthlySpendData(User user) {
        try {
            LocalDateTime startDate = LocalDateTime.now().minusMonths(12);
            List<Object[]> results = orderRepository.getMonthlySpendData(user, startDate);
            
            // Create map of all months for the last 12 months
            Map<String, DashboardStatsDTO.MonthlySpendDTO> monthMap = new LinkedHashMap<>();
            LocalDateTime current = LocalDateTime.now().minusMonths(12);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            
            for (int i = 0; i < 12; i++) {
                String monthKey = current.format(formatter);
                monthMap.put(monthKey, new DashboardStatsDTO.MonthlySpendDTO(monthKey, BigDecimal.ZERO, 0L));
                current = current.plusMonths(1);
            }
            
            // Fill in actual data
            for (Object[] row : results) {
                // Handle different database date formats
                String monthKey;
                Object monthObj = row[0];
                if (monthObj instanceof java.sql.Timestamp) {
                    LocalDateTime date = ((java.sql.Timestamp) monthObj).toLocalDateTime();
                    monthKey = date.format(formatter);
                } else if (monthObj instanceof LocalDateTime) {
                    monthKey = ((LocalDateTime) monthObj).format(formatter);
                } else {
                    monthKey = monthObj.toString().substring(0, 7); // Assuming string format "yyyy-MM-..."
                }
                
                BigDecimal amount = row[1] instanceof BigDecimal ? (BigDecimal) row[1] : 
                                   new BigDecimal(row[1].toString());
                Long orderCount = ((Number) row[2]).longValue();
                
                monthMap.put(monthKey, new DashboardStatsDTO.MonthlySpendDTO(monthKey, amount, orderCount));
            }
            
            return new ArrayList<>(monthMap.values());
            
        } catch (Exception e) {
            logger.error("Error getting monthly spend data: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get spending by category (for pie chart)
     */
    private List<DashboardStatsDTO.CategorySpendDTO> getCategorySpendData(User user, BigDecimal totalSpent) {
        try {
            List<Object[]> results = orderRepository.getSpendingByCategory(user);
            
            List<DashboardStatsDTO.CategorySpendDTO> categorySpendList = new ArrayList<>();
            
            for (Object[] row : results) {
                String categoryName = row[0] != null ? (String) row[0] : "Uncategorized";
                BigDecimal amount = row[1] instanceof BigDecimal ? (BigDecimal) row[1] : 
                                   new BigDecimal(row[1].toString());
                Long orderCount = ((Number) row[2]).longValue();
                
                // Calculate percentage
                Double percentage = 0.0;
                if (totalSpent != null && totalSpent.compareTo(BigDecimal.ZERO) > 0) {
                    percentage = (amount.doubleValue() / totalSpent.doubleValue()) * 100;
                }
                
                categorySpendList.add(new DashboardStatsDTO.CategorySpendDTO(
                    categoryName, amount, orderCount, percentage
                ));
            }
            
            return categorySpendList;
            
        } catch (Exception e) {
            logger.error("Error getting category spend data: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get spending by platform (for platform distribution)
     */
    private List<DashboardStatsDTO.PlatformDistributionDTO> getPlatformDistribution(User user, BigDecimal totalSpent) {
        try {
            List<Object[]> results = orderRepository.getSpendingByPlatform(user);
            
            List<DashboardStatsDTO.PlatformDistributionDTO> platformDistList = new ArrayList<>();
            
            for (Object[] row : results) {
                String platformName = row[0] != null ? (String) row[0] : "Unknown Platform";
                BigDecimal amount = row[1] instanceof BigDecimal ? (BigDecimal) row[1] : 
                                   new BigDecimal(row[1].toString());
                Long orderCount = ((Number) row[2]).longValue();
                
                // Calculate percentage
                Double percentage = 0.0;
                if (totalSpent != null && totalSpent.compareTo(BigDecimal.ZERO) > 0) {
                    percentage = (amount.doubleValue() / totalSpent.doubleValue()) * 100;
                }
                
                platformDistList.add(new DashboardStatsDTO.PlatformDistributionDTO(
                    platformName, amount, orderCount, percentage
                ));
            }
            
            return platformDistList;
            
        } catch (Exception e) {
            logger.error("Error getting platform distribution data: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Get active orders (not DELIVERED and not CANCELLED)
     */
    private List<DashboardStatsDTO.ActiveOrderDTO> getActiveOrdersList(User user) {
        try {
            List<Order> activeOrders = orderRepository.getActiveOrders(user);
            
            return activeOrders.stream().map(order -> 
                new DashboardStatsDTO.ActiveOrderDTO(
                    order.getId(),
                    order.getOrderId(),
                    order.getPlatform() != null ? order.getPlatform().getPlatformName() : "N/A",
                    order.getCategory() != null ? order.getCategory().getCategoryName() : "Uncategorized",
                    order.getPrice(),
                    order.getShipmentStatus(),
                    order.getOrderDate(),
                    order.getDeliveryDate()
                )
            ).collect(Collectors.toList());
            
        } catch (Exception e) {
            logger.error("Error getting active orders list: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
}
