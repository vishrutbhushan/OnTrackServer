package com.project.onTrackServer.Models;

import java.util.List;

public interface IEntity<T extends BaseEntity> {
    T create(Long userId, String name) throws Exception;
    List<T> findByUser(Long userId) throws Exception;
    boolean delete(Long userId, Long entityId) throws Exception;
}