package com.project.onTrackServer.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.project.onTrackServer.Models.Notifications;
import com.project.onTrackServer.Models.Order;
import com.project.onTrackServer.Models.User;
import com.project.onTrackServer.Models.UserConfig;
import com.project.onTrackServer.jdbc.JdbcManager;

import java.sql.Connection;
import java.time.LocalDateTime;

@Service
public class SchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);

    private final ParserService parserService;

    public SchedulerService(ParserService parserService) {
        this.parserService = parserService;
    }

    @Scheduled(fixedRate = 300_000)
    public void processEmails() {
        try {
            List<User> users = User.findAll();
            for (User user : users) {
                List<Email> emails = Email.fetchEmailsForProcessing(user);
                for (Email email : emails) {
                    
                    email.setUser(user);
                    List<Order> orders = parserService.extractOrdersFromEmail(email);
                    for (Order order : orders) {
                        
                        order.setUser(user);
                        order.save();
                        Notifications not = new Notifications(user, order);
                        not.sendOrderNotification();
                    }
                    email.archive();
                }
                UserConfig.updateLastProcessedEmailTime(user, LocalDateTime.now());
            }
            logger.debug("Triggered processEmails");
        } catch (Exception e) {
            logger.error("Error in processEmails", e);
        }
    }
}
