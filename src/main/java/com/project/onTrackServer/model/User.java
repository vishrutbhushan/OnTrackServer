package com.project.onTrackServer.model;

import java.sql.*;
import com.project.onTrackServer.jdbc.JdbcManager;

public class User {
    private Long id;
    private String userId;
    private String email;
    private String displayName;
    private String accessToken;
    private String fcmToken;
    private Boolean isDeleted;

    
    public User() {}
    public User(Long id, String userId, String email, String displayName, String accessToken, String fcmToken, Boolean isDeleted) {
        this.id = id;
        this.userId = userId;
        this.email = email;
        this.displayName = displayName;
        this.accessToken = accessToken;
        this.fcmToken = fcmToken;
        this.isDeleted = isDeleted;
    }

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }
    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }

    
    public User findByUserId(String userId) throws SQLException {
        Connection conn = JdbcManager.getInstance().getConnection();
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

    public User findByEmail(String email) throws SQLException {
        Connection conn = JdbcManager.getInstance().getConnection();
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

    public User create(User user) throws SQLException {
        Connection conn = JdbcManager.getInstance().getConnection();
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

    public User update(User user) throws SQLException {
        Connection conn = JdbcManager.getInstance().getConnection();
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

    public User updateAccessToken(String userId, String accessToken) throws SQLException {
        Connection conn = JdbcManager.getInstance().getConnection();
        String sql = "UPDATE users SET access_token = ? WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, accessToken);
            stmt.setString(2, userId);
            stmt.executeUpdate();
            return findByUserId(userId);
        }
    }

    public User updateFcmToken(String userId, String fcmToken) throws SQLException {
        Connection conn = JdbcManager.getInstance().getConnection();
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
        return u;
    }
}

