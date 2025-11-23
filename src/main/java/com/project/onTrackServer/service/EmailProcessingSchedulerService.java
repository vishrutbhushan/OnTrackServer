package com.project.onTrackServer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class EmailProcessingSchedulerService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailProcessingSchedulerService.class);
    
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final ScheduledExecutorService scheduler;

    public EmailProcessingSchedulerService(EmailService emailService, NotificationService notificationService) {
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.scheduler = Executors.newScheduledThreadPool(1);
        
        startScheduler();
        logger.info("EmailProcessingSchedulerService initialized with 3-service architecture");
    }

    private void startScheduler() {
        logger.info("Starting email processing scheduler...");
        scheduler.scheduleAtFixedRate(this::processEmails, 0, 5, TimeUnit.MINUTES);
    }

    private void processEmails() {
        logger.info("=== 3-Service Architecture Test ===");
        logger.info("1. EmailProcessingSchedulerService: ✅ Running");
        logger.info("2. EmailService: {}", emailService != null ? "✅ Injected" : "❌ Failed");
        logger.info("3. NotificationService: {}", notificationService != null ? "✅ Injected" : "❌ Failed");
        
        // Test notification service
        try {
            notificationService.sendNotification("test-user", "Test", "3-Service Architecture Working!");
        } catch (Exception e) {
            logger.info("NotificationService test (expected to need Firebase config): {}", e.getMessage());
        }
    }

    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            logger.info("EmailProcessingSchedulerService shutdown completed");
        }
    }
}
