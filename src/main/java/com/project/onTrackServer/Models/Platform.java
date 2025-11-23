package com.project.onTrackServer.Models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.project.onTrackServer.jdbc.JdbcManager;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Platform extends BaseEntity implements IEntity<Platform> {
    private String platformName;
    private Double platformRating;

    private static final Connection conn = JdbcManager.getInstance().getConnection();

    @Override
    public List<Platform> findByUser(Long userId) throws SQLException {
        String sql = "SELECT * FROM platform WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            List<Platform> platforms = new ArrayList<>();
            while (rs.next()) {
                platforms.add(fromResultSet(rs));
            }
            return platforms;
        }
    }

    public static List<Platform> findByUserAndIsDeletedFalse(Long userId) throws SQLException {
        String sql = "SELECT * FROM platform WHERE user_id = ? AND is_deleted = false";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            List<Platform> platforms = new ArrayList<>();
            while (rs.next()) {
                platforms.add(fromResultSet(rs));
            }
            return platforms;
        }
    }

    public static Platform findByIdAndUser(Long id, Long userId) throws SQLException {
        String sql = "SELECT * FROM platform WHERE id = ? AND user_id = ?";
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
    public Platform create(Long userId, String platformName) throws SQLException {
        String sql = "INSERT INTO platform (user_id, platform_name, is_deleted) VALUES (?, ?, false)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, userId);
            stmt.setString(2, platformName);
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                Platform p = new Platform();
                p.setId(keys.getLong(1));
                p.setUserId(userId);
                p.setPlatformName(platformName);
                p.setIsDeleted(false);
                return p;
            }
        }
        return null;
    }

    @Override
    public boolean delete(Long userId, Long platformId) throws SQLException {
        String sql = "UPDATE platform SET is_deleted = true WHERE id = ? AND user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, platformId);
            stmt.setLong(2, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    private static Platform fromResultSet(ResultSet rs) throws SQLException {
        Platform p = new Platform();
        p.setId(rs.getLong("id"));
        p.setUserId(rs.getLong("user_id"));
        p.setPlatformName(rs.getString("platform_name"));
        p.setPlatformRating(rs.getObject("platform_rating") != null ? rs.getDouble("platform_rating") : null);
        p.setIsDeleted(rs.getBoolean("is_deleted"));
        return p;
    }
}
