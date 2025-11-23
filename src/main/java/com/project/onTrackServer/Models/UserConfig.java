package com.project.onTrackServer.Models;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class UserConfig {
    private Long id;
    private Long userId;
    private Integer pollingFrequency;
    private Boolean notificationEnabled;
    private LocalDateTime lastProcessedEmailTime;
    private Boolean autoArchiveOrderEmails;
    private Boolean isDeleted;
    
    public static UserConfig findByUser(Connection conn, Long userId) throws SQLException {
        String sql = "SELECT * FROM user_config WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return fromResultSet(rs);
            }
            return null;
        }
    }

    public static List<UserConfig> findByUserAndIsDeletedFalse(Connection conn, Long userId) throws SQLException {
        String sql = "SELECT * FROM user_config WHERE user_id = ? AND is_deleted = false";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            List<UserConfig> configs = new ArrayList<>();
            while (rs.next()) {
                configs.add(fromResultSet(rs));
            }
            return configs;
        }
    }

    public static UserConfig fromResultSet(ResultSet rs) throws SQLException {
        UserConfig uc = new UserConfig();
        uc.setId(rs.getLong("id"));
        uc.setUserId(rs.getLong("user_id"));
        uc.setPollingFrequency(rs.getInt("polling_frequency"));
        uc.setNotificationEnabled(rs.getBoolean("notification_enabled"));
        Timestamp ts = rs.getTimestamp("last_processed_email_time");
        uc.setLastProcessedEmailTime(ts != null ? ts.toLocalDateTime() : null);
        uc.setAutoArchiveOrderEmails(rs.getBoolean("auto_archive_order_emails"));
        uc.setIsDeleted(rs.getBoolean("is_deleted"));
        return uc;
    }

    public static void updateLastProcessedEmailTime(Connection conn, Long userId, LocalDateTime time) throws SQLException {
        String sql = "UPDATE user_config SET last_processed_email_time = ? WHERE user_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(time));
            stmt.setLong(2, userId);
            stmt.executeUpdate();
        }
    }

    public void save(Connection conn) throws SQLException {
        String sql = "INSERT INTO user_config (user_id, polling_frequency, notification_enabled, last_processed_email_time, auto_archive_order_emails, is_deleted) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, this.userId);
            stmt.setInt(2, this.pollingFrequency != null ? this.pollingFrequency : 5);
            stmt.setBoolean(3, this.notificationEnabled != null ? this.notificationEnabled : true);
            stmt.setTimestamp(4, this.lastProcessedEmailTime != null ? Timestamp.valueOf(this.lastProcessedEmailTime) : null);
            stmt.setBoolean(5, this.autoArchiveOrderEmails != null ? this.autoArchiveOrderEmails : true);
            stmt.setBoolean(6, this.isDeleted != null ? this.isDeleted : false);
            stmt.executeUpdate();
        }
    }
}
