package com.project.onTrackServer.repository;

import com.project.onTrackServer.model.UserNotification;
import com.project.onTrackServer.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    
    Optional<UserNotification> findByUserAndIsDeletedFalse(User user);
    
    @Query("SELECT un FROM UserNotification un WHERE un.user.id = :userId AND un.isDeleted = false")
    Optional<UserNotification> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT un FROM UserNotification un WHERE un.user.id = :userId AND un.isDeleted = false")
    List<UserNotification> findByUserIdAndUnread(@Param("userId") Long userId);
    
    @Query("SELECT un FROM UserNotification un WHERE un.user.id = :userId AND un.isDeleted = false")
    List<UserNotification> findByUserIdAndRead(@Param("userId") Long userId);
    
    @Query("SELECT un FROM UserNotification un WHERE un.user.id = :userId AND un.createTime >= :since AND un.isDeleted = false")
    List<UserNotification> findRecentNotifications(@Param("userId") Long userId, @Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(un) FROM UserNotification un WHERE un.user.id = :userId AND un.isDeleted = false")
    Long countUnreadNotifications(@Param("userId") Long userId);
    
    @Query("UPDATE UserNotification un SET un.isDeleted = true WHERE un.notificationId = :notificationId")
    void markAsRead(@Param("notificationId") Long notificationId);
    
    @Query("UPDATE UserNotification un SET un.isDeleted = true WHERE un.user.id = :userId")
    void markAllAsReadForUser(@Param("userId") Long userId);
    
    @Query("DELETE FROM UserNotification un WHERE un.createTime < :cutoffDate")
    void deleteOldNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    @Query("SELECT un FROM UserNotification un WHERE un.user.id = :userId AND un.isDeleted = false")
    Page<UserNotification> findByUserIdPaginated(@Param("userId") Long userId, Pageable pageable);
    
    boolean existsByUserAndIsDeletedFalse(User user);
    
    void deleteByUser(User user);
}
