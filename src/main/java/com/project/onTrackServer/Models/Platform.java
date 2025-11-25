package com.project.onTrackServer.Models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.project.onTrackServer.jdbc.JdbcManager;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Platform extends BaseEntity implements Entity<Platform> {
    private String platformName;
    private Double platformRating;

    public Platform() {
        this.isDeleted = false;
    }

    @Override
    public List<Platform> findByUser(User user) throws SQLException {
        String sql = "SELECT * FROM platform WHERE user_id = ?";
        try (Connection conn = JdbcManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, user.getId());
            ResultSet rs = stmt.executeQuery();
            List<Platform> platforms = new ArrayList<>();
            while (rs.next()) {
                platforms.add(fromResultSet(rs));
            }
            return platforms;
        }
    }

    public static List<Platform> findByUserAndIsDeletedFalse(User user) throws SQLException {
        String sql = "SELECT * FROM platform WHERE user_id = ? AND is_deleted = false";
        try (Connection conn = JdbcManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, user.getId());
            ResultSet rs = stmt.executeQuery();
            List<Platform> platforms = new ArrayList<>();
            while (rs.next()) {
                platforms.add(fromResultSet(rs));
            }
            return platforms;
        }
    }

    public static Platform findByIdAndUser(Long id, User user) throws SQLException {
        String sql = "SELECT * FROM platform WHERE id = ? AND user_id = ?";
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

    @Override
    public Platform create(User user, String platformName) throws SQLException {
        String sql = "INSERT INTO platform (user_id, platform_name, is_deleted) VALUES (?, ?, false)";
        try (Connection conn = JdbcManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, user.getId());
            stmt.setString(2, platformName);
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                Platform p = new Platform();
                p.setId(keys.getLong(1));
                p.setUser(user);
                p.setPlatformName(platformName);
                p.setIsDeleted(false);
                return p;
            }
        }
        return null;
    }

    @Override
    public boolean delete(User user, Long platformId) throws SQLException {
        String sql = "UPDATE platform SET is_deleted = true WHERE id = ? AND user_id = ?";
        try (Connection conn = JdbcManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, platformId);
            stmt.setLong(2, user.getId());
            return stmt.executeUpdate() > 0;
        }
    }

    private static Platform fromResultSet(ResultSet rs) throws SQLException {
        Platform p = new Platform();
        p.setId(rs.getLong("id"));
        Long userId = rs.getLong("user_id");
        if (userId != null) {
            User user = User.findByUserId(String.valueOf(userId));
            p.setUser(user);
        }
        p.setPlatformName(rs.getString("platform_name"));
        p.setPlatformRating(rs.getObject("platform_rating") != null ? rs.getDouble("platform_rating") : null);
        p.setIsDeleted(rs.getBoolean("is_deleted"));
        return p;
    }
}
