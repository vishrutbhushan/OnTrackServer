package com.project.onTrackServer.model;

import java.util.List;

/**
 * Generic entity interface. Implementing classes should provide typed
 * implementations for create/find/delete operations.
 */
public interface IEntity<T extends BaseEntity> {
    T create(Long userId, String name) throws Exception;
    List<T> findByUser(Long userId) throws Exception;
    boolean delete(Long userId, Long entityId) throws Exception;

    Long getId();
    void setId(Long id);
    Long getUserId();
    void setUserId(Long userId);
    String getName();
    void setName(String name);
    Boolean getIsDeleted();
    void setIsDeleted(Boolean isDeleted);
}