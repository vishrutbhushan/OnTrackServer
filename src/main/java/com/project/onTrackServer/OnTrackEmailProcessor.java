package com.project.onTrackServer;

import com.project.onTrackServer.service.EmailProcessingSchedulerService;
import com.project.onTrackServer.service.EmailService;
import com.project.onTrackServer.service.NotificationService;

import java.security.GeneralSecurityException;
import java.io.IOException;

public class OnTrackEmailProcessor {

    private EmailProcessingSchedulerService schedulerService;

    public static void main(String[] args) {
        OnTrackEmailProcessor app = new OnTrackEmailProcessor();
        try {
            app.start();
            Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
            Thread.currentThread().join();
        } catch (Exception e) {
            System.exit(1);
        }
    }

    public void start() throws GeneralSecurityException, IOException {

        EmailService emailService = new EmailService();
        NotificationService notificationService = new NotificationService();
        schedulerService = new EmailProcessingSchedulerService(
                emailService,
                notificationService);

    }

    public void stop() {
        if (schedulerService != null) {
            schedulerService.shutdown();
        }
    }
}
