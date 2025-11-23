package com.project.onTrackServer.model;

import java.util.List;

public abstract class BaseEntity {
    protected Long id;
    protected Long userId;
    protected String name;
    protected Boolean isDeleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }

    
    public abstract BaseEntity create(Long userId, String name) throws Exception;
    public abstract List<? extends BaseEntity> findByUser(Long userId) throws Exception;
    public abstract boolean delete(Long userId, Long entityId) throws Exception;
}