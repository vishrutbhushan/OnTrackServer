package com.project.onTrackServer.Models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.project.onTrackServer.jdbc.JdbcManager;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Category extends BaseEntity implements IEntity<Category> {
    private String categoryName;

    private static final Connection conn = JdbcManager.getInstance().getConnection();

    @Override
    public List<Category> findByUser(Long userId) throws SQLException {
        String sql = "SELECT * FROM category WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            List<Category> categories = new ArrayList<>();
            while (rs.next()) {
                categories.add(fromResultSet(rs));
            }
            return categories;
        }
    }

    public static List<Category> findByUserAndIsDeletedFalse(Long userId) throws SQLException {
        String sql = "SELECT * FROM category WHERE user_id = ? AND is_deleted = false";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            List<Category> categories = new ArrayList<>();
            while (rs.next()) {
                categories.add(fromResultSet(rs));
            }
            return categories;
        }
    }

    public static Category findByIdAndUser(Long id, Long userId) throws SQLException {
        String sql = "SELECT * FROM category WHERE id = ? AND user_id = ?";
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

    @Override
    public Category create(Long userId, String categoryName) throws SQLException {
        String sql = "INSERT INTO category (user_id, category_name, is_deleted) VALUES (?, ?, false)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, userId);
            stmt.setString(2, categoryName);
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                Category c = new Category();
                c.setId(keys.getLong(1));
                c.setUserId(userId);
                c.setCategoryName(categoryName);
                c.setIsDeleted(false);
                return c;
            }
        }
        return null;
    }

    @Override
    public boolean delete(Long userId, Long categoryId) throws SQLException {
        String sql = "UPDATE category SET is_deleted = true WHERE id = ? AND user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, categoryId);
            stmt.setLong(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    public static Category fromResultSet(ResultSet rs) throws SQLException {
        Category c = new Category();
        c.setId(rs.getLong("id"));
        c.setUserId(rs.getLong("user_id"));
        c.setCategoryName(rs.getString("category_name"));
        c.setIsDeleted(rs.getBoolean("is_deleted"));
        return c;
    }
}
