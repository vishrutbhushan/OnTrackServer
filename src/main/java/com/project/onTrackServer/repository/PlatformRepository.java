package com.project.onTrackServer.repository;

import com.project.onTrackServer.model.Platform;
import com.project.onTrackServer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlatformRepository extends JpaRepository<Platform, Long> {
    List<Platform> findByUser(User user);
    List<Platform> findByUserAndIsDeletedFalse(User user);
    Optional<Platform> findByIdAndUser(Long id, User user);
}
