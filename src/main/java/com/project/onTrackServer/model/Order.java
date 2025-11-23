package com.project.onTrackServer.model;

import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.project.onTrackServer.jdbc.JdbcManager;

/**
 * Order entity with manual JDBC methods for CRUD and queries.
 */
public class Order {
    private Long id;
    private Long userId;
    private Long platformId;
    private Long categoryId;
    private String orderId;
    private BigDecimal price;
    private Integer quantity;
    private Boolean isDeleted;

    // --- Constructors ---
    public Order() {}
    public Order(Long id, Long userId, Long platformId, Long categoryId, String orderId, BigDecimal price, Integer quantity, Boolean isDeleted) {
        this.id = id;
        this.userId = userId;
        this.platformId = platformId;
        this.categoryId = categoryId;
        this.orderId = orderId;
        this.price = price;
        this.quantity = quantity;
        this.isDeleted = isDeleted;
    }

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getPlatformId() { return platformId; }
    public void setPlatformId(Long platformId) { this.platformId = platformId; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }

    // --- JDBC Methods ---
    public static Order findByOrderId(String orderId) throws SQLException {
        Connection conn = JdbcManager.getInstance().getConnection();
        String sql = "SELECT * FROM orders WHERE order_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, orderId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return fromResultSet(rs);
            }
            return null;
        }
    }

    public static List<Order> findByUser(Long userId) throws SQLException {
        Connection conn = JdbcManager.getInstance().getConnection();
        String sql = "SELECT * FROM orders WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            List<Order> orders = new ArrayList<>();
            while (rs.next()) {
                orders.add(fromResultSet(rs));
            }
            return orders;
        }
    }

    public static List<Order> findByUserAndIsDeletedFalse(Long userId) throws SQLException {
        Connection conn = JdbcManager.getInstance().getConnection();
        String sql = "SELECT * FROM orders WHERE user_id = ? AND is_deleted = false";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            List<Order> orders = new ArrayList<>();
            while (rs.next()) {
                orders.add(fromResultSet(rs));
            }
            return orders;
        }
    }

    public static Order findByIdAndUser(Long id, Long userId) throws SQLException {
        Connection conn = JdbcManager.getInstance().getConnection();
        String sql = "SELECT * FROM orders WHERE id = ? AND user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setLong(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return fromResultSet(rs);
            }
            return null;
        }
    }

    public static Order fromResultSet(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getLong("id"));
        o.setUserId(rs.getLong("user_id"));
        o.setPlatformId(rs.getObject("platform_id") != null ? rs.getLong("platform_id") : null);
        o.setCategoryId(rs.getObject("category_id") != null ? rs.getLong("category_id") : null);
        o.setOrderId(rs.getString("order_id"));
        o.setPrice(rs.getBigDecimal("price"));
        o.setQuantity(rs.getInt("quantity"));
        o.setIsDeleted(rs.getBoolean("is_deleted"));
        return o;
    }

    // --- Additional JDBC Query Methods (from old repository) ---

    public static BigDecimal getTotalSpent(Long userId) throws SQLException {
        Connection conn = JdbcManager.getInstance().getConnection();
        String sql = "SELECT COALESCE(SUM(price), 0) FROM orders WHERE user_id = ? AND is_deleted = false";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
            return BigDecimal.ZERO;
        }
    }

    public static BigDecimal getTotalSpentLast30Days(Long userId, java.time.LocalDateTime thirtyDaysAgo) throws SQLException {
        Connection conn = JdbcManager.getInstance().getConnection();
        String sql = "SELECT COALESCE(SUM(price), 0) FROM orders WHERE user_id = ? AND is_deleted = false AND order_date >= ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setTimestamp(2, Timestamp.valueOf(thirtyDaysAgo));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
            return BigDecimal.ZERO;
        }
    }

    public static long countActiveOrders(Long userId) throws SQLException {
        Connection conn = JdbcManager.getInstance().getConnection();
        String sql = "SELECT COUNT(*) FROM orders WHERE user_id = ? AND is_deleted = false AND shipment_status NOT IN ('DELIVERED', 'CANCELLED')";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        }
    }

    public static List<Object[]> getMonthlySpendData(Long userId, java.time.LocalDateTime startDate) throws SQLException {
        Connection conn = JdbcManager.getInstance().getConnection();
        String sql = "SELECT DATE_FORMAT(order_date, '%Y-%m') as month, COALESCE(SUM(price), 0) as totalAmount, COUNT(*) as orderCount FROM orders WHERE user_id = ? AND is_deleted = false AND order_date >= ? GROUP BY DATE_FORMAT(order_date, '%Y-%m') ORDER BY month ASC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setTimestamp(2, Timestamp.valueOf(startDate));
            ResultSet rs = stmt.executeQuery();
            List<Object[]> result = new ArrayList<>();
            while (rs.next()) {
                result.add(new Object[]{rs.getString("month"), rs.getBigDecimal("totalAmount"), rs.getInt("orderCount")});
            }
            return result;
        }
    }

    public static List<Object[]> getSpendingByCategory(Long userId) throws SQLException {
        Connection conn = JdbcManager.getInstance().getConnection();
        String sql = "SELECT c.category_name, COALESCE(SUM(o.price), 0) as totalAmount, COUNT(o.id) as orderCount FROM orders o LEFT JOIN category c ON o.category_id = c.id WHERE o.user_id = ? AND o.is_deleted = false GROUP BY c.category_name ORDER BY totalAmount DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            List<Object[]> result = new ArrayList<>();
            while (rs.next()) {
                result.add(new Object[]{rs.getString("category_name"), rs.getBigDecimal("totalAmount"), rs.getInt("orderCount")});
            }
            return result;
        }
    }

    public static List<Object[]> getSpendingByPlatform(Long userId) throws SQLException {
        Connection conn = JdbcManager.getInstance().getConnection();
        String sql = "SELECT p.platform_name, COALESCE(SUM(o.price), 0) as totalAmount, COUNT(o.id) as orderCount FROM orders o LEFT JOIN platform p ON o.platform_id = p.id WHERE o.user_id = ? AND o.is_deleted = false GROUP BY p.platform_name ORDER BY totalAmount DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            List<Object[]> result = new ArrayList<>();
            while (rs.next()) {
                result.add(new Object[]{rs.getString("platform_name"), rs.getBigDecimal("totalAmount"), rs.getInt("orderCount")});
            }
            return result;
        }
    }

    public static List<Order> getActiveOrders(Long userId) throws SQLException {
        Connection conn = JdbcManager.getInstance().getConnection();
        String sql = "SELECT * FROM orders WHERE user_id = ? AND is_deleted = false AND shipment_status NOT IN ('DELIVERED', 'CANCELLED') ORDER BY order_date DESC";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            List<Order> orders = new ArrayList<>();
            while (rs.next()) {
                orders.add(fromResultSet(rs));
            }
            return orders;
        }
    }
}