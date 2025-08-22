package com.project.onTrackServer.repository;

import com.project.onTrackServer.model.FCMToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FCMTokenRepository extends JpaRepository<FCMToken, Long> {
    
    Optional<FCMToken> findByUserId(String userId);
    
    void deleteByUserId(String userId);
}
