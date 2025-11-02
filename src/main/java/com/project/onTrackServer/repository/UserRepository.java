package com.project.onTrackServer.repository;

import com.project.onTrackServer.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.isDeleted = false")
    List<User> findAllActive();
    
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isDeleted = false")
    Optional<User> findByEmailAndNotDeleted(@Param("email") String email);
    
    @Query("SELECT u FROM User u WHERE u.userId = :userId AND u.isDeleted = false")
    Optional<User> findByUserIdAndNotDeleted(@Param("userId") Long userId);
    
    @Query("SELECT u FROM User u WHERE u.name LIKE %:name% AND u.isDeleted = false")
    List<User> findByNameContainingAndNotDeleted(@Param("name") String name);
    
    @Query("SELECT u FROM User u WHERE u.pushNotificationEnabled = true AND u.isDeleted = false")
    List<User> findUsersWithNotificationsEnabled();
}
