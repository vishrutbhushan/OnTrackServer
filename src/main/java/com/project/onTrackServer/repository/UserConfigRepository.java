package com.project.onTrackServer.repository;

import com.project.onTrackServer.model.UserConfig;
import com.project.onTrackServer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserConfigRepository extends JpaRepository<UserConfig, Long> {
    Optional<UserConfig> findByUser(User user);
    List<UserConfig> findByUserAndIsDeletedFalse(User user);
}
