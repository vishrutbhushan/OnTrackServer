package com.project.onTrackServer.Models;

import java.util.List;

public interface Entity<T extends BaseEntity> {
    T create(User user, String name) throws Exception;
    List<T> findByUser(User user) throws Exception;
    boolean delete(User user, Long entityId) throws Exception;
}