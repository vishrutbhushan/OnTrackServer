package com.project.onTrackServer;

import com.project.onTrackServer.service.EmailProcessingSchedulerService;
import com.project.onTrackServer.service.EmailService;
import com.project.onTrackServer.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.GeneralSecurityException;
import java.io.IOException;

/**
 * Main application class for OnTrack Email Processing Server
 * Runs without Spring Boot - pure object-oriented approach
 */
public class OnTrackEmailProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(OnTrackEmailProcessor.class);
    
    private EmailProcessingSchedulerService schedulerService;
    
    public static void main(String[] args) {
        OnTrackEmailProcessor app = new OnTrackEmailProcessor();
        
        try {
            app.start();
            
            // Keep the application running
            logger.info("OnTrack Email Processor started successfully");
            logger.info("Press Ctrl+C to stop the application");
            
            Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
            
            // Keep the main thread alive
            Thread.currentThread().join();
            
        } catch (Exception e) {
            logger.error("Failed to start OnTrack Email Processor: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
    
    /**
     * Start the email processing services
     */
    public void start() throws GeneralSecurityException, IOException {
        logger.info("Starting OnTrack Email Processor...");
        
        // Initialize services (3-service architecture)
        EmailService emailService = new EmailService();
        NotificationService notificationService = new NotificationService();
        
        // Initialize and start the scheduler service (Gemini AI integrated into this service)
        schedulerService = new EmailProcessingSchedulerService(
            emailService, 
            notificationService
        );
        
        logger.info("OnTrack Email Processor initialization completed - 3-Service Architecture");
    }
    
    /**
     * Stop the email processing services
     */
    public void stop() {
        logger.info("Stopping OnTrack Email Processor...");
        
        if (schedulerService != null) {
            schedulerService.shutdown();
        }
        
        logger.info("OnTrack Email Processor stopped");
    }
}
