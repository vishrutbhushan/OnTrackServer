package com.project.onTrackServer.Models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.project.onTrackServer.jdbc.JdbcManager;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)

public class Category extends BaseEntity implements Entity<Category> {
    private static final Connection connection = JdbcManager.getInstance().getConnection();
    private String categoryName;

    public Category() {
        this.isDeleted = false;
    }

    @Override
    public List<Category> findByUser(User user) throws SQLException {
        String sql = "SELECT * FROM category WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, user.getId());
            ResultSet rs = stmt.executeQuery();
            List<Category> categories = new ArrayList<>();
            while (rs.next()) {
                categories.add(fromResultSet(rs));
            }
            return categories;
        }
    }

    public static List<Category> findByUserAndIsDeletedFalse(User user) throws SQLException {
        String sql = "SELECT * FROM category WHERE user_id = ? AND is_deleted = false";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, user.getId());
            ResultSet rs = stmt.executeQuery();
            List<Category> categories = new ArrayList<>();
            while (rs.next()) {
                categories.add(fromResultSet(rs));
            }
            return categories;
        }
    }

    public static Category findByIdAndUser(Long id, User user) throws SQLException {
        String sql = "SELECT * FROM category WHERE id = ? AND user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setLong(2, user.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return fromResultSet(rs);
            }
            return null;
        }
    }

    @Override
    public Category create(User user, String categoryName) throws SQLException {
        String sql = "INSERT INTO category (user_id, category_name, is_deleted) VALUES (?, ?, false)";
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, user.getId());
            stmt.setString(2, categoryName);
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                Category c = new Category();
                c.setId(keys.getLong(1));
                c.setUser(user);
                c.setCategoryName(categoryName);
                c.setIsDeleted(false);
                return c;
            }
        }
        return null;
    }

    @Override
    public boolean delete(User user, Long categoryId) throws SQLException {
        String sql = "UPDATE category SET is_deleted = true WHERE id = ? AND user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, categoryId);
            stmt.setLong(2, user.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    public static Category fromResultSet(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getLong("id"));
        Long userId = rs.getLong("user_id");
        if (userId != null) {
            User user = User.findByUserId(String.valueOf(userId)); 
            c.setUser(user);
        }
        c.setCategoryName(rs.getString("category_name"));
        c.setIsDeleted(rs.getBoolean("is_deleted"));
        return c;
    }
}
