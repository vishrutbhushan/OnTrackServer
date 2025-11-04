package com.project.onTrackServer.dto;

import com.project.onTrackServer.model.User;
import com.project.onTrackServer.model.UserConfig;

public class UserDTO {
    private Long id;
    private String userId;
    private String email;
    private String displayName;
    private String accessToken;
    private String fcmToken;
    
    // UserConfig fields
    private Integer pollingFrequency;
    private Boolean notificationEnabled;
    private Boolean autoArchiveOrderEmails;
    
    public UserDTO() {}
    
    public UserDTO(User user) {
        this.id = user.getId();
        this.userId = user.getUserId();
        this.email = user.getEmail();
        this.displayName = user.getDisplayName();
        this.accessToken = user.getAccessToken();
        this.fcmToken = user.getFcmToken();
        
        // Set defaults
        this.pollingFrequency = 2;
        this.notificationEnabled = true;
        this.autoArchiveOrderEmails = true;
        
        // Override with config if available
        if (user.getUserConfig() != null) {
            UserConfig config = user.getUserConfig();
            if (config.getPollingFrequency() != null) {
                this.pollingFrequency = config.getPollingFrequency();
            }
            if (config.getNotificationEnabled() != null) {
                this.notificationEnabled = config.getNotificationEnabled();
            }
            if (config.getAutoArchiveOrderEmails() != null) {
                this.autoArchiveOrderEmails = config.getAutoArchiveOrderEmails();
            }
        }
    }
    
    // Getters and Setters
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
    
    public Integer getPollingFrequency() { return pollingFrequency; }
    public void setPollingFrequency(Integer pollingFrequency) { this.pollingFrequency = pollingFrequency; }
    
    public Boolean getNotificationEnabled() { return notificationEnabled; }
    public void setNotificationEnabled(Boolean notificationEnabled) { this.notificationEnabled = notificationEnabled; }
    
    public Boolean getAutoArchiveOrderEmails() { return autoArchiveOrderEmails; }
    public void setAutoArchiveOrderEmails(Boolean autoArchiveOrderEmails) { this.autoArchiveOrderEmails = autoArchiveOrderEmails; }
}
