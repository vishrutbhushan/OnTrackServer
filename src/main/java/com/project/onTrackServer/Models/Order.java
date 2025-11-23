package com.project.onTrackServer.Models;

import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.project.onTrackServer.jdbc.JdbcManager;
import lombok.Data;

@Data
public class Order {
    private Long id;
    private User user;
    private Platform platform;
    private Category category;
    private String orderId;
    private BigDecimal price;
    private Integer quantity;
    private String shipmentStatus;
    private LocalDateTime orderDate;
    private Boolean isDeleted;
    
    public Order() {
        this.isDeleted = false;
        this.orderDate = LocalDateTime.now();
        this.quantity = 1;
    }
    
    public static Order findByOrderId(String orderId) throws SQLException {
        String sql = "SELECT * FROM orders WHERE order_id = ?";
        try (Connection conn = JdbcManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, orderId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return fromResultSet(rs);
            }
            return null;
        }
    }

    public static List<Order> findByUser(User user) throws SQLException {
        String sql = "SELECT * FROM orders WHERE user_id = ?";
        try (Connection conn = JdbcManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, user.getId());
            ResultSet rs = stmt.executeQuery();
            List<Order> orders = new ArrayList<>();
            while (rs.next()) {
                orders.add(fromResultSet(rs));
            }
            return orders;
        }
    }

    public static List<Order> findByUserAndIsDeletedFalse(User user) throws SQLException {
        String sql = "SELECT * FROM orders WHERE user_id = ? AND is_deleted = false";
        try (Connection conn = JdbcManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, user.getId());
            ResultSet rs = stmt.executeQuery();
            List<Order> orders = new ArrayList<>();
            while (rs.next()) {
                orders.add(fromResultSet(rs));
            }
            return orders;
        }
    }

    public static Order findByIdAndUser(Long id, User user) throws SQLException {
        String sql = "SELECT * FROM orders WHERE id = ? AND user_id = ?";
        try (Connection conn = JdbcManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setLong(2, user.getId());
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
        Long userId = rs.getLong("user_id");
        if (userId != null) {
            User user = User.findByUserId(String.valueOf(userId));
            o.setUser(user);
        }
        Long platformId = rs.getObject("platform_id") != null ? rs.getLong("platform_id") : null;
        if (platformId != null) {
            Platform platform = Platform.findByIdAndUser(platformId, o.getUser());
            o.setPlatform(platform);
        }
        Long categoryId = rs.getObject("category_id") != null ? rs.getLong("category_id") : null;
        if (categoryId != null) {
            Category category = Category.findByIdAndUser(categoryId, o.getUser());
            o.setCategory(category);
        }
        o.setOrderId(rs.getString("order_id"));
        o.setPrice(rs.getBigDecimal("price"));
        o.setQuantity(rs.getInt("quantity"));
        o.setShipmentStatus(rs.getString("shipment_status"));
        Timestamp orderDateTs = rs.getTimestamp("order_date");
        o.setOrderDate(orderDateTs != null ? orderDateTs.toLocalDateTime() : null);
        o.setIsDeleted(rs.getBoolean("is_deleted"));
        return o;
    }

    public static BigDecimal getTotalSpent(Long userId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(price), 0) FROM orders WHERE user_id = ? AND is_deleted = false";
        try (Connection conn = JdbcManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
            return BigDecimal.ZERO;
        }
    }

    public static BigDecimal getTotalSpentLast30Days(Long userId, LocalDateTime thirtyDaysAgo) throws SQLException {
        String sql = "SELECT COALESCE(SUM(price), 0) FROM orders WHERE user_id = ? AND is_deleted = false AND order_date >= ?";
        try (Connection conn = JdbcManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
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
        String sql = "SELECT COUNT(*) FROM orders WHERE user_id = ? AND is_deleted = false AND shipment_status NOT IN ('DELIVERED', 'CANCELLED')";
        try (Connection conn = JdbcManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
            return 0;
        }
    }

    public static List<Object[]> getMonthlySpendData(Long userId, LocalDateTime startDate) throws SQLException {
        String sql = "SELECT DATE_FORMAT(order_date, '%Y-%m') as month, COALESCE(SUM(price), 0) as totalAmount, COUNT(*) as orderCount FROM orders WHERE user_id = ? AND is_deleted = false AND order_date >= ? GROUP BY DATE_FORMAT(order_date, '%Y-%m') ORDER BY month ASC";
        try (Connection conn = JdbcManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
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
        String sql = "SELECT c.category_name, COALESCE(SUM(o.price), 0) as totalAmount, COUNT(o.id) as orderCount FROM orders o LEFT JOIN category c ON o.category_id = c.id WHERE o.user_id = ? AND o.is_deleted = false GROUP BY c.category_name ORDER BY totalAmount DESC";
        try (Connection conn = JdbcManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
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
        String sql = "SELECT p.platform_name, COALESCE(SUM(o.price), 0) as totalAmount, COUNT(o.id) as orderCount FROM orders o LEFT JOIN platform p ON o.platform_id = p.id WHERE o.user_id = ? AND o.is_deleted = false GROUP BY p.platform_name ORDER BY totalAmount DESC";
        try (Connection conn = JdbcManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
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
        String sql = "SELECT * FROM orders WHERE user_id = ? AND is_deleted = false AND shipment_status NOT IN ('DELIVERED', 'CANCELLED') ORDER BY order_date DESC";
        try (Connection conn = JdbcManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
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
            String sql = "INSERT INTO orders (user_id, platform_id, category_id, order_id, price, quantity, shipment_status, order_date, is_deleted) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ";
            try (Connection conn = JdbcManager.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, this.user.getId());
                if (this.platform != null && this.platform.getId() != null) stmt.setLong(2, this.platform.getId()); else stmt.setNull(2, Types.BIGINT);
                if (this.category != null && this.category.getId() != null) stmt.setLong(3, this.category.getId()); else stmt.setNull(3, Types.BIGINT);
                stmt.setString(4, this.orderId);
                stmt.setBigDecimal(5, this.price);
                stmt.setInt(6, this.quantity != null ? this.quantity : 1);
                stmt.setString(7, this.shipmentStatus);
                stmt.setTimestamp(8, Timestamp.valueOf(this.orderDate != null ? this.orderDate : LocalDateTime.now()));
                stmt.setBoolean(9, this.isDeleted != null ? this.isDeleted : false);
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