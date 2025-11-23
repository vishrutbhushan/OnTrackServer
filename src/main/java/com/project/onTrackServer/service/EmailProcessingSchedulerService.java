package com.project.onTrackServer.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class EmailProcessingSchedulerService {
    
    
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final ScheduledExecutorService scheduler;

    public EmailProcessingSchedulerService(EmailService emailService, NotificationService notificationService) {
        this.emailService = emailService;
        this.notificationService = notificationService;
        this.scheduler = Executors.newScheduledThreadPool(1);
        
        startScheduler();
    }

    private void startScheduler() {
        
        scheduler.scheduleAtFixedRate(this::processEmails, 0, 5, TimeUnit.MINUTES);
    }

    private void processEmails() {
        
        
        
        try {
            notificationService.sendNotification("test-user", "Test", "3-Service Architecture Working!");
        } catch (Exception e) {
            
        }
    }

    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            
        }
    }
}
