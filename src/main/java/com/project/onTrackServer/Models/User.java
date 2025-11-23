package com.project.onTrackServer.Models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.project.onTrackServer.jdbc.JdbcManager;

import lombok.Data;

@Data
public class User {
    private Long id;
    private String userId;
    private String email;
    private String displayName;
    private String accessToken;
    private String fcmToken;
    private Boolean isDeleted;
    private UserConfig userConfig;

    private static final Connection conn = JdbcManager.getInstance().getConnection();

    public static User findByUserId(String userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return fromResultSet(rs);
            }
            return null;
        }
    }

    public static User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return fromResultSet(rs);
            }
            return null;
        }
    }

    public static User create(User user) throws SQLException {
        String sql = "INSERT INTO users (user_id, email, display_name, access_token, fcm_token, is_deleted) VALUES (?, ?, ?, ?, ?, false)";
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUserId());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getDisplayName());
            stmt.setString(4, user.getAccessToken());
            stmt.setString(5, user.getFcmToken());
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                user.setId(keys.getLong(1));
                user.setIsDeleted(false);
            }
            return user;
        }
    }

    public static User update(User user) throws SQLException {
        String sql = "UPDATE users SET email = ?, display_name = ?, access_token = ?, fcm_token = ? WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getDisplayName());
            stmt.setString(3, user.getAccessToken());
            stmt.setString(4, user.getFcmToken());
            stmt.setString(5, user.getUserId());
            stmt.executeUpdate();
            return user;
        }
    }

    public static User updateAccessToken(String userId, String accessToken) throws SQLException {
        String sql = "UPDATE users SET access_token = ? WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, accessToken);
            stmt.setString(2, userId);
            stmt.executeUpdate();
            return findByUserId(userId);
        }
    }

    public static User updateFcmToken(String userId, String fcmToken) throws SQLException {
        String sql = "UPDATE users SET fcm_token = ? WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fcmToken);
            stmt.setString(2, userId);
            stmt.executeUpdate();
            return findByUserId(userId);
        }
    }

    private static User fromResultSet(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setUserId(rs.getString("user_id"));
        u.setEmail(rs.getString("email"));
        u.setDisplayName(rs.getString("display_name"));
        u.setAccessToken(rs.getString("access_token"));
        u.setFcmToken(rs.getString("fcm_token"));
        u.setIsDeleted(rs.getBoolean("is_deleted"));
        // Load userConfig
        u.setUserConfig(UserConfig.findByUser(conn, u.getId()));
        return u;
    }

    public static List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM users";
        List<User> users = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(fromResultSet(rs));
            }
        }
        return users;
    }
}

