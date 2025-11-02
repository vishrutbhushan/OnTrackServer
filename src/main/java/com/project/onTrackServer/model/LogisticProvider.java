package com.project.onTrackServer.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "logistic_provider")
public class LogisticProvider {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "logistic_id")
    private Long logisticId;
    
    @Column(name = "name", nullable = false, unique = true)
    private String name;
    
    @Column(name = "avg_rating", precision = 3, scale = 2)
    private BigDecimal avgRating;
    
    @Column(name = "api_endpoint", length = 500)
    private String apiEndpoint;
    
    @Column(name = "can_delay")
    private Integer canDelay;
    
    @Column(name = "can_be_bad_quality")
    private Integer canBeBadQuality;
    
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
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
    
    // Constructors
    public LogisticProvider() {}
    
    public LogisticProvider(String name) {
        this.name = name;
    }
    
    // Getters and Setters
    public Long getLogisticId() { return logisticId; }
    public void setLogisticId(Long logisticId) { this.logisticId = logisticId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public BigDecimal getAvgRating() { return avgRating; }
    public void setAvgRating(BigDecimal avgRating) { this.avgRating = avgRating; }
    
    public String getApiEndpoint() { return apiEndpoint; }
    public void setApiEndpoint(String apiEndpoint) { this.apiEndpoint = apiEndpoint; }
    
    public Integer getCanDelay() { return canDelay; }
    public void setCanDelay(Integer canDelay) { this.canDelay = canDelay; }
    
    public Integer getCanBeBadQuality() { return canBeBadQuality; }
    public void setCanBeBadQuality(Integer canBeBadQuality) { this.canBeBadQuality = canBeBadQuality; }
    
    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
    
    public Long getCreateUser() { return createUser; }
    public void setCreateUser(Long createUser) { this.createUser = createUser; }
    
    public Long getUpdateUser() { return updateUser; }
    public void setUpdateUser(Long updateUser) { this.updateUser = updateUser; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    
    @Override
    public String toString() {
        return "LogisticProvider{" +
                "logisticId=" + logisticId +
                ", name='" + name + '\'' +
                ", avgRating=" + avgRating +
                ", canDelay=" + canDelay +
                ", canBeBadQuality=" + canBeBadQuality +
                '}';
    }
}
