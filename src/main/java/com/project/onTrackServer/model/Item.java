package com.project.onTrackServer.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import lombok.Data;

@Data
@Entity
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String subject;
    private String snippet;
    
    @Column(columnDefinition = "LONGTEXT")
    private String body; // Full email body
    
    private String sender;
    private String userId;
    private String orderId;
    
    @Column(name = "gmail_message_id", unique = true)
    private String gmailMessageId; // Gmail message ID for archiving and tracking
}
