package com.project.onTrackServer.Models;

import java.util.List;

import lombok.Data;

@Data
public abstract class BaseEntity {
    protected Long id;
    protected User user;
    protected String name;
    protected Boolean isDeleted;

    public abstract BaseEntity create(User user, String name) throws Exception;
    public abstract List<? extends BaseEntity> findByUser(User user) throws Exception;
    public abstract boolean delete(User user, Long entityId) throws Exception;
}