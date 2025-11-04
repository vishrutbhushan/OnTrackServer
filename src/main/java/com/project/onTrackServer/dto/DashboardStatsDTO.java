package com.project.onTrackServer.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for Dashboard Statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    
    // KVP Cards
    private long totalOrders;
    private BigDecimal totalSpent;
    private BigDecimal last30DaysSpent;
    private long activeOrders;
    
    // Line Chart Data - Money spent vs month
    private List<MonthlySpendDTO> monthlySpendData;
    
    // Pie Chart Data - Category Spent
    private List<CategorySpendDTO> categorySpendData;
    
    // Platform Distribution
    private List<PlatformDistributionDTO> platformDistribution;
    
    // Active Orders List
    private List<ActiveOrderDTO> activeOrdersList;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlySpendDTO {
        private String month;  // Format: "2024-11" (YYYY-MM)
        private BigDecimal amount;
        private long orderCount;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySpendDTO {
        private String categoryName;
        private BigDecimal amount;
        private long orderCount;
        private Double percentage;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlatformDistributionDTO {
        private String platformName;
        private BigDecimal amount;
        private long orderCount;
        private Double percentage;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveOrderDTO {
        private Long orderId;
        private String orderCode;
        private String platformName;
        private String categoryName;
        private BigDecimal price;
        private String shipmentStatus;
        
        @JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, 
                   pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        private LocalDateTime orderDate;
        
        @JsonFormat(shape = com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING, 
                   pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        private LocalDateTime deliveryDate;
    }
}
