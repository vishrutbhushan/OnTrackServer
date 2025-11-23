package com.project.onTrackServer.Models;

import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.project.onTrackServer.jdbc.JdbcManager;
import lombok.Data;

@Data
public class Order {
    private Long id;
    private Long userId;
    private Long platformId;
    private Long categoryId;
    private String orderId;
    private BigDecimal price;
    private Integer quantity;
    private String shipmentStatus;
    private Boolean isDeleted;
    
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

    public void save() {
        try {
            Connection conn = JdbcManager.getInstance().getConnection();
            String sql = "INSERT INTO orders (user_id, platform_id, category_id, order_id, price, quantity, shipment_status, is_deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?) ";
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, this.userId);
                if (this.platformId != null) stmt.setLong(2, this.platformId); else stmt.setNull(2, Types.BIGINT);
                if (this.categoryId != null) stmt.setLong(3, this.categoryId); else stmt.setNull(3, Types.BIGINT);
                stmt.setString(4, this.orderId);
                stmt.setBigDecimal(5, this.price);
                stmt.setInt(6, this.quantity != null ? this.quantity : 1);
                stmt.setString(7, this.shipmentStatus);
                stmt.setBoolean(8, this.isDeleted != null ? this.isDeleted : false);
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating order failed, no rows affected.");
                }
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        this.id = generatedKeys.getLong(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save order: " + e.getMessage(), e);
        }
    }
}