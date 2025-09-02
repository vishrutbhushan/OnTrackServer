package com.project.onTrackServer.service;

import com.project.onTrackServer.model.Item;
import com.project.onTrackServer.model.Token;
import com.project.onTrackServer.repository.ItemRepository;
import com.project.onTrackServer.repository.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
@ConditionalOnProperty(name = "email.processing.schedule.enabled", havingValue = "true", matchIfMissing = true)
public class EmailProcessingSchedulerService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailProcessingSchedulerService.class);
    
    @Autowired
    private TokenRepository tokenRepository;
    
    @Autowired
    private ItemRepository itemRepository;
    
    @Autowired
    private GmailService gmailService;
    
    @Autowired
    private GeminiEmailAnalysisService emailAnalysisService;
    
    @Value("${email.processing.schedule.interval:10}")
    private int intervalMinutes;
    
    @Scheduled(fixedDelayString = "#{${email.processing.schedule.interval:10} * 60 * 1000}") // Convert minutes to milliseconds
    public void processEmails() {
        logger.info("Starting scheduled email processing...");
        
        try {
            List<Token> allTokens = tokenRepository.findAll();
            
            for (Token token : allTokens) {
                if (hasValidAccessToken(token)) {
                    processEmailsForUser(token);
                }
            }
            
            logger.info("Completed scheduled email processing for {} users", allTokens.size());
            
        } catch (Exception e) {
            logger.error("Error during scheduled email processing: {}", e.getMessage(), e);
        }
    }
    
    private void processEmailsForUser(Token token) {
        try {
            logger.info("Processing emails for user: {}", token.getUserId());
            
            List<Item> newEmails = gmailService.fetchNewEmailsFromGmail(token, itemRepository);
            
            if (!newEmails.isEmpty()) {
                logger.info("Found {} new emails for user: {}", newEmails.size(), token.getUserId());
                
                for (Item email : newEmails) {
                    processAndSaveEmail(email);
                }
                
            } else {
                logger.debug("No new emails found for user: {}", token.getUserId());
            }
            
        } catch (Exception e) {
            logger.error("Error processing emails for user {}: {}", token.getUserId(), e.getMessage());
        }
    }
    
    private void processAndSaveEmail(Item email) {
        try {
            // Analyze email with Gemini AI
            GeminiEmailAnalysisService.EmailAnalysisResult analysis = 
                emailAnalysisService.analyzeEmail(email.getSnippet(), email.getSubject(), email.getSender());
            
            // Only save order-related emails
            if (analysis.isOrderRelatedEmail()) {
                email.setOrderId(analysis.getOrderId());
                
                // Store the order ID in snippet field for now as requested
                String originalSnippet = email.getSnippet();
                String enhancedSnippet = analysis.getOrderId() != null ? 
                    "Order ID: " + analysis.getOrderId() + " | " + originalSnippet : originalSnippet;
                email.setSnippet(enhancedSnippet);
                
                itemRepository.save(email);
                logger.info("Saved order-related email for user: {} with order ID: {}", 
                    email.getUserId(), analysis.getOrderId());
            } else {
                logger.debug("Skipped non-order email for user: {}", email.getUserId());
            }
            
        } catch (Exception e) {
            logger.error("Error processing email: {}", e.getMessage());
        }
    }
    
    private boolean hasValidAccessToken(Token token) {
        return token != null && 
               token.getAccessToken() != null && 
               !token.getAccessToken().equals("gmail_access_granted") &&
               !token.getAccessToken().trim().isEmpty();
    }
}
