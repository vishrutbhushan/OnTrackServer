package com.project.onTrackServer.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;

@Entity
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String subject;
    private String snippet;
    private String sender;
    private String userId;
    
    @Column(name = "gmail_message_id", unique = true)
    private String gmailMessageId; // To track processed emails
    
    @Column(name = "order_id")
    private String orderId; // Extracted order ID

    public Item() {}

    public Item(String subject, String snippet, String sender, String userId) {
        this.subject = subject;
        this.snippet = snippet;
        this.sender = sender;
        this.userId = userId;
    }
    
    public Item(String subject, String snippet, String sender, String userId, String gmailMessageId, String orderId) {
        this.subject = subject;
        this.snippet = snippet;
        this.sender = sender;
        this.userId = userId;
        this.gmailMessageId = gmailMessageId;
        this.orderId = orderId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getGmailMessageId() {
        return gmailMessageId;
    }
    
    public void setGmailMessageId(String gmailMessageId) {
        this.gmailMessageId = gmailMessageId;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
}
