package com.project.onTrackServer.controller;

import com.project.onTrackServer.dto.DashboardStatsDTO;
import com.project.onTrackServer.model.User;
import com.project.onTrackServer.repository.UserRepository;
import com.project.onTrackServer.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

/**
 * REST Controller for Dashboard APIs
 */
@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DashboardController {
    
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    
    @Autowired
    private DashboardService dashboardService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get user from request headers or session
     */
    private User getUserFromRequest(HttpServletRequest request) {
        // Get userId from header (set by authentication filter/interceptor)
        String userId = request.getHeader("X-User-Id");
        if (userId == null || userId.isEmpty()) {
            userId = request.getHeader("userId");
        }
        
        if (userId != null && !userId.isEmpty()) {
            Optional<User> user = userRepository.findByUserId(userId);
            return user.orElse(null);
        }
        return null;
    }
    
    /**
     * Get comprehensive dashboard statistics
     * GET /api/dashboard/stats
     * 
     * Response includes:
     * - Total Orders (KVP Card)
     * - Total Spent (KVP Card)
     * - Last 30 Days Spent (KVP Card)
     * - Active Orders Count (KVP Card)
     * - Monthly spending data (line chart)
     * - Category spending data (pie chart)
     * - Platform distribution
     * - List of active orders
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getDashboardStats(HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            if (user == null) {
                logger.warn("Unauthorized access to dashboard stats");
                return ResponseEntity.status(401).body("Unauthorized");
            }
            
            logger.info("Fetching dashboard stats for user: {}", user.getUserId());
            
            DashboardStatsDTO stats = dashboardService.getDashboardStats(user);
            
            logger.info("Dashboard stats retrieved successfully for user: {}", user.getUserId());
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            logger.error("Error fetching dashboard stats: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error fetching dashboard statistics: " + e.getMessage());
        }
    }
    
    /**
     * Get key performance indicators (KVP Cards)
     * GET /api/dashboard/kpi
     * 
     * Returns only the 4 KVP card values:
     * - totalOrders
     * - totalSpent
     * - last30DaysSpent
     * - activeOrders
     */
    @GetMapping("/kpi")
    public ResponseEntity<?> getKPIMetrics(HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            if (user == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }
            
            logger.info("Fetching KPI metrics for user: {}", user.getUserId());
            
            DashboardStatsDTO stats = dashboardService.getDashboardStats(user);
            
            // Create a KPI-only response
            KPIResponse kpiResponse = new KPIResponse(
                stats.getTotalOrders(),
                stats.getTotalSpent(),
                stats.getLast30DaysSpent(),
                stats.getActiveOrders()
            );
            
            return ResponseEntity.ok(kpiResponse);
            
        } catch (Exception e) {
            logger.error("Error fetching KPI metrics: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error fetching KPI metrics: " + e.getMessage());
        }
    }
    
    /**
     * Get monthly spending chart data
     * GET /api/dashboard/monthly-spending
     * 
     * Returns line chart data for last 12 months
     */
    @GetMapping("/monthly-spending")
    public ResponseEntity<?> getMonthlySpending(HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            if (user == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }
            
            logger.info("Fetching monthly spending data for user: {}", user.getUserId());
            
            DashboardStatsDTO stats = dashboardService.getDashboardStats(user);
            
            return ResponseEntity.ok(stats.getMonthlySpendData());
            
        } catch (Exception e) {
            logger.error("Error fetching monthly spending data: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error fetching monthly spending data: " + e.getMessage());
        }
    }
    
    /**
     * Get category spending data
     * GET /api/dashboard/category-spending
     * 
     * Returns pie chart data for spending by category
     */
    @GetMapping("/category-spending")
    public ResponseEntity<?> getCategorySpending(HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            if (user == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }
            
            logger.info("Fetching category spending data for user: {}", user.getUserId());
            
            DashboardStatsDTO stats = dashboardService.getDashboardStats(user);
            
            return ResponseEntity.ok(stats.getCategorySpendData());
            
        } catch (Exception e) {
            logger.error("Error fetching category spending data: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error fetching category spending data: " + e.getMessage());
        }
    }
    
    /**
     * Get platform distribution data
     * GET /api/dashboard/platform-distribution
     * 
     * Returns spending breakdown by platform (Amazon, Flipkart, etc.)
     */
    @GetMapping("/platform-distribution")
    public ResponseEntity<?> getPlatformDistribution(HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            if (user == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }
            
            logger.info("Fetching platform distribution data for user: {}", user.getUserId());
            
            DashboardStatsDTO stats = dashboardService.getDashboardStats(user);
            
            return ResponseEntity.ok(stats.getPlatformDistribution());
            
        } catch (Exception e) {
            logger.error("Error fetching platform distribution data: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error fetching platform distribution data: " + e.getMessage());
        }
    }
    
    /**
     * Get active orders
     * GET /api/dashboard/active-orders
     * 
     * Returns list of active orders (not DELIVERED and not CANCELLED)
     */
    @GetMapping("/active-orders")
    public ResponseEntity<?> getActiveOrders(HttpServletRequest request) {
        try {
            User user = getUserFromRequest(request);
            if (user == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }
            
            logger.info("Fetching active orders for user: {}", user.getUserId());
            
            DashboardStatsDTO stats = dashboardService.getDashboardStats(user);
            
            return ResponseEntity.ok(stats.getActiveOrdersList());
            
        } catch (Exception e) {
            logger.error("Error fetching active orders: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Error fetching active orders: " + e.getMessage());
        }
    }
    
    /**
     * Inner class for KPI Response
     */
    public static class KPIResponse {
        public long totalOrders;
        public java.math.BigDecimal totalSpent;
        public java.math.BigDecimal last30DaysSpent;
        public long activeOrders;
        
        public KPIResponse(long totalOrders, java.math.BigDecimal totalSpent, 
                          java.math.BigDecimal last30DaysSpent, long activeOrders) {
            this.totalOrders = totalOrders;
            this.totalSpent = totalSpent;
            this.last30DaysSpent = last30DaysSpent;
            this.activeOrders = activeOrders;
        }
    }
}
