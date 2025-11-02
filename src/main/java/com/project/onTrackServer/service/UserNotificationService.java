package com.project.onTrackServer.service;

import com.project.onTrackServer.model.UserNotification;
import com.project.onTrackServer.model.User;
import com.project.onTrackServer.repository.UserNotificationRepository;
import com.project.onTrackServer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class UserNotificationService {

    private final UserNotificationRepository userNotificationRepository;
    private final UserRepository userRepository;

    @Autowired
    public UserNotificationService(UserNotificationRepository userNotificationRepository, UserRepository userRepository) {
        this.userNotificationRepository = userNotificationRepository;
        this.userRepository = userRepository;
    }

    public UserNotification save(UserNotification notification) {
        return userNotificationRepository.save(notification);
    }

    public Optional<UserNotification> findById(Long id) {
        return userNotificationRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<UserNotification> findByUserId(Long userId) {
        return userNotificationRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Optional<UserNotification> findByUser(User user) {
        return userNotificationRepository.findByUserAndIsDeletedFalse(user);
    }

    public UserNotification createUserNotificationSettings(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        User user = userOpt.get();
        
        // Check if notification settings already exist
        Optional<UserNotification> existing = userNotificationRepository.findByUserAndIsDeletedFalse(user);
        if (existing.isPresent()) {
            return existing.get();
        }

        UserNotification notification = new UserNotification(user);
        notification.setDeclutter(false);
        notification.setNotifications(true);
        return userNotificationRepository.save(notification);
    }

    public UserNotification updateNotificationSettings(Long userId, Boolean declutter, Boolean notifications) {
        Optional<UserNotification> notificationOpt = findByUserId(userId);
        
        if (notificationOpt.isEmpty()) {
            // Create new settings if they don't exist
            return createUserNotificationSettings(userId);
        }

        UserNotification notification = notificationOpt.get();
        
        if (declutter != null) {
            notification.setDeclutter(declutter);
        }
        if (notifications != null) {
            notification.setNotifications(notifications);
        }
        notification.setUpdateTime(LocalDateTime.now());
        
        return userNotificationRepository.save(notification);
    }

    public void deleteUserNotificationSettings(Long userId, Long deleteUserId) {
        Optional<UserNotification> notificationOpt = findByUserId(userId);
        
        if (notificationOpt.isPresent()) {
            UserNotification notification = notificationOpt.get();
            notification.setIsDeleted(true);
            notification.setUpdateUser(deleteUserId);
            notification.setUpdateTime(LocalDateTime.now());
            userNotificationRepository.save(notification);
        }
    }

    public boolean hasNotificationSettingsForUser(User user) {
        return userNotificationRepository.existsByUserAndIsDeletedFalse(user);
    }
}
