package com.project.onTrackServer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * UserConfig entity to store user-specific configuration settings
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_config")
public class UserConfig extends AuditBase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    @Column(name = "polling_frequency", nullable = false)
    private Integer pollingFrequency; // 15, 30, 60, 120 (in seconds)
    
    @Column(name = "notification_enabled", nullable = false)
    private Boolean notificationEnabled = true;
    
    @Column(name = "last_processed_email_id")
    private String lastProcessedEmailId; // Gmail message ID of the last processed email
    
    @Column(name = "last_processed_email_time")
    private LocalDateTime lastProcessedEmailTime; // Timestamp of the last processed email
    
    @Column(name = "auto_archive_order_emails", nullable = false)
    private Boolean autoArchiveOrderEmails = true; // Whether to archive emails recognized as order-related
}
