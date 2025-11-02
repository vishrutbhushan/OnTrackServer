package com.project.onTrackServer.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "user_id", unique = true)
    private String userId;
    
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "age")
    private Integer age;
    
    @Column(name = "auth_token", columnDefinition = "TEXT")
    private String authToken;
    
    @Column(name = "fcm_token", columnDefinition = "TEXT")
    private String fcmToken;
    
    @Column(name = "push_notification_enabled")
    private Boolean pushNotificationEnabled = true;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "polling_frequency")
    private PollingFrequency pollingFrequency = PollingFrequency.THIRTY_MINUTES;
    
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
    
    @Column(name = "create_user")
    private Long createUser;
    
    @Column(name = "update_user")
    private Long updateUser;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    // Relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserVendorMap> userVendorMaps;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Orders> orders;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserNotification userNotification;
    
    public enum PollingFrequency {
        FIFTEEN_MINUTES("15 minutes"),
        THIRTY_MINUTES("30 minutes"),
        ONE_HOUR("1 hour"),
        TWO_HOURS("2 hours");
        
        private final String value;
        
        PollingFrequency(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    public User() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
    
    public User(String email, String name) {
        this();
        this.email = email;
        this.name = name;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updateTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getAge() {
        return age;
    }
    
    public void setAge(Integer age) {
        this.age = age;
    }
    
    public String getAuthToken() {
        return authToken;
    }
    
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
    
    public String getFcmToken() {
        return fcmToken;
    }
    
    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
    
    public Boolean getPushNotificationEnabled() {
        return pushNotificationEnabled;
    }
    
    public void setPushNotificationEnabled(Boolean pushNotificationEnabled) {
        this.pushNotificationEnabled = pushNotificationEnabled;
    }
    
    public PollingFrequency getPollingFrequency() {
        return pollingFrequency;
    }
    
    public void setPollingFrequency(PollingFrequency pollingFrequency) {
        this.pollingFrequency = pollingFrequency;
    }
    
    public Boolean getIsDeleted() {
        return isDeleted;
    }
    
    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }
    
    public Long getCreateUser() {
        return createUser;
    }
    
    public void setCreateUser(Long createUser) {
        this.createUser = createUser;
    }
    
    public Long getUpdateUser() {
        return updateUser;
    }
    
    public void setUpdateUser(Long updateUser) {
        this.updateUser = updateUser;
    }
    
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    public Set<UserVendorMap> getUserVendorMaps() {
        return userVendorMaps;
    }
    
    public void setUserVendorMaps(Set<UserVendorMap> userVendorMaps) {
        this.userVendorMaps = userVendorMaps;
    }
    
    public Set<Orders> getOrders() {
        return orders;
    }
    
    public void setOrders(Set<Orders> orders) {
        this.orders = orders;
    }
    
    public UserNotification getUserNotification() {
        return userNotification;
    }
    
    public void setUserNotification(UserNotification userNotification) {
        this.userNotification = userNotification;
    }
}
