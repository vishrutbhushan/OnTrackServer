package com.project.onTrackServer.model;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserConfig {
    private Long id;
    private Long userId;
    private Integer pollingFrequency;
    private Boolean notificationEnabled;
    private LocalDateTime lastProcessedEmailTime;
    private Boolean autoArchiveOrderEmails;
    private Boolean isDeleted;

    
    public UserConfig() {}
    public UserConfig(Long id, Long userId, Integer pollingFrequency, Boolean notificationEnabled, LocalDateTime lastProcessedEmailTime, Boolean autoArchiveOrderEmails, Boolean isDeleted) {
        this.id = id;
        this.userId = userId;
        this.pollingFrequency = pollingFrequency;
        this.notificationEnabled = notificationEnabled;
        this.lastProcessedEmailTime = lastProcessedEmailTime;
        this.autoArchiveOrderEmails = autoArchiveOrderEmails;
        this.isDeleted = isDeleted;
    }

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Integer getPollingFrequency() { return pollingFrequency; }
    public void setPollingFrequency(Integer pollingFrequency) { this.pollingFrequency = pollingFrequency; }
    public Boolean getNotificationEnabled() { return notificationEnabled; }
    public void setNotificationEnabled(Boolean notificationEnabled) { this.notificationEnabled = notificationEnabled; }
    public LocalDateTime getLastProcessedEmailTime() { return lastProcessedEmailTime; }
    public void setLastProcessedEmailTime(LocalDateTime lastProcessedEmailTime) { this.lastProcessedEmailTime = lastProcessedEmailTime; }
    public Boolean getAutoArchiveOrderEmails() { return autoArchiveOrderEmails; }
    public void setAutoArchiveOrderEmails(Boolean autoArchiveOrderEmails) { this.autoArchiveOrderEmails = autoArchiveOrderEmails; }
    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }

    
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

    
}
