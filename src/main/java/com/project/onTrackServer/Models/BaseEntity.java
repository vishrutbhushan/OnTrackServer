package com.project.onTrackServer.Models;

import java.util.List;

import lombok.Data;

@Data
public abstract class BaseEntity {
    protected Long id;
    protected Long userId;
    protected String name;
    protected Boolean isDeleted;
    
    public abstract BaseEntity create(Long userId, String name) throws Exception;
    public abstract List<? extends BaseEntity> findByUser(Long userId) throws Exception;
    public abstract boolean delete(Long userId, Long entityId) throws Exception;
}