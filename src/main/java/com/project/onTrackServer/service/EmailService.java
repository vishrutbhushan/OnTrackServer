 
package com.project.onTrackServer.service;

import com.project.onTrackServer.model.User;
import com.project.onTrackServer.model.Order;
import com.project.onTrackServer.model.Platform;
import com.project.onTrackServer.model.Category;
import com.project.onTrackServer.model.Vendor;
import com.project.onTrackServer.repository.UserRepository;
import com.project.onTrackServer.repository.OrderRepository;
import com.project.onTrackServer.repository.UserConfigRepository;
import com.project.onTrackServer.repository.PlatformRepository;
import com.project.onTrackServer.repository.CategoryRepository;
import com.project.onTrackServer.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

@Service
@ConditionalOnProperty(name = "email.processing.schedule.enabled", havingValue = "true", matchIfMissing = true)
public class EmailProcessingSchedulerService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailProcessingSchedulerService.class);
    
    @Value("${email.processing.schedule.interval:10}")
    private int intervalMinutes;
    
    @Scheduled(fixedDelayString = "#{${email.processing.schedule.interval:10} * 60 * 1000}")
    public void processEmails() {
        logger.info("Starting scheduled email processing...");
        
        try {
            List<User> allUsers = userRepository.findAll();
            
            for (User user : allUsers) {
                if (hasValidAccessToken(user)) {
                    processEmailsForUser(user);
                }
            }
            
            logger.info("Completed scheduled email processing for {} users", allUsers.size());
            
        } catch (Exception e) {
            logger.error("Error during scheduled email processing: {}", e.getMessage(), e);
        }
    }
    
    private void processEmailsForUser(User user) {
        try {
            logger.info("Processing emails for user: {}", user.getUserId());
            
            
            List<Platform> userPlatforms = platformRepository.findByUserAndIsDeletedFalse(user);
            logger.debug("User has {} platforms", userPlatforms.size());
            
            
            List<Category> userCategories = categoryRepository.findByUserAndIsDeletedFalse(user);
            List<String> categoryNames = userCategories.stream()
                .map(Category::getCategoryName)
                .toList();
            logger.debug("User has {} categories: {}", userCategories.size(), categoryNames);
            
            
            processUserEmails(user, userPlatforms, categoryNames);
            
        } catch (Exception e) {
            logger.error("Error processing emails for user {}: {}", user.getUserId(), e.getMessage());
        }
    }
    
    private void processUserEmails(User user, List<Platform> userPlatforms, List<String> categoryNames) {
        try {
            logger.debug("Fetching Gmail messages for user: {}", user.getUserId());
            
            String accessToken = user.getAccessToken();
            if (accessToken == null || "gmail_access_granted".equals(accessToken)) {
                logger.warn("No valid Gmail access token for user: {}", user.getUserId());
                return;
            }
                        try {
                processGmailMessagesDirectly(user, userPlatforms, categoryNames);
            } catch (Exception e) {
                logger.error("Error fetching Gmail messages for user {}: {}", user.getUserId(), e.getMessage());
            }
            
        } catch (Exception e) {
            logger.error("Error processing user emails for {}: {}", user.getUserId(), e.getMessage());
        }
    }
    
    private void processGmailMessagesDirectly(User user, List<Platform> userPlatforms, List<String> categoryNames) {
        try {
            logger.debug("Processing Gmail messages directly for user: {}", user.getUserId());
            
            List<GmailService.EmailData> emails = gmailService.fetchEmailsForProcessing(user);
            
            if (!emails.isEmpty()) {
                logger.info("Processing {} NEW emails for user: {}", emails.size(), user.getUserId());
                
                for (GmailService.EmailData emailData : emails) {
                    
                    if (!isPlatformAllowed(emailData.sender, userPlatforms)) {
                        logger.info("Email from {} skipped - not in user's allowed platforms", emailData.sender);
                        continue;
                    }
                    
                    
                    processEmailDirectly(emailData, user, userPlatforms, categoryNames);
                }
                
                
                updateLastProcessedEmailTimestamp(user);
                
            } else {
                logger.debug("No new emails found for user: {}", user.getUserId());
            }
            
        } catch (Exception e) {
            logger.error("Error in direct Gmail processing for user {}: {}", user.getUserId(), e.getMessage());
        }
    }
    
    private void processEmailDirectly(GmailService.EmailData emailData, User user, List<Platform> userPlatforms, List<String> categoryNames) {
        try {
            logger.info("Processing email from sender: {} with subject: {} for user: {}", 
                emailData.sender, emailData.subject, user.getUserId());
            
                    
            logger.debug("Sending email to Gemini for analysis - Subject: {}, Sender: {}", 
                emailData.subject, emailData.sender);
            
            GeminiEmailAnalysisService.EmailAnalysisResult analysis = 
                emailAnalysisService.analyzeEmail(emailData.body != null ? emailData.body : emailData.snippet, 
                    emailData.subject, emailData.sender, categoryNames);
            
            logger.info("Gemini analysis result - isOrderRelated: {}, orderId: {}, isNewOrder: {}, shipmentStatus: {}", 
                analysis.isOrderRelatedEmail(), analysis.getOrderId(), analysis.isNewOrder(), analysis.getShipmentStatus());
            
            
            if (analysis.isOrderRelatedEmail()) {
                logger.info("Email identified as order-related");
                
                
                Platform matchingPlatform = findMatchingPlatform(emailData.sender, userPlatforms);
                logger.debug("Matching platform: {}", matchingPlatform != null ? matchingPlatform.getPlatformName() : "None");
                
                
                handleOrderCreationOrUpdate(user, analysis, matchingPlatform);
                
                
                if (user.getUserConfig() != null && 
                    user.getUserConfig().getAutoArchiveOrderEmails() != null &&
                    user.getUserConfig().getAutoArchiveOrderEmails()) {
                    
                    logger.debug("Auto-archive is enabled for user: {}", user.getUserId());
                    
                    if (emailData.messageId != null) {
                        gmailService.archiveEmail(user, emailData.messageId);
                        logger.info("Archived order email for user: {} with message ID: {}", 
                            user.getUserId(), emailData.messageId);
                    } else {
                        logger.warn("Gmail message ID is null, cannot archive email for user: {}", user.getUserId());
                    }
                } else {
                    logger.debug("Auto-archive is disabled for user: {}", user.getUserId());
                }
                
            } else {
                logger.info("Email skipped - NOT order-related from sender: {}", emailData.sender);
            }
            
        } catch (Exception e) {
            logger.error("Error processing email from sender {} for user {}: {}", 
                emailData.sender, user.getUserId(), e.getMessage(), e);
        }
    }
    
    private boolean isPlatformAllowed(String senderEmail, List<Platform> userPlatforms) {
        if (senderEmail == null || senderEmail.isEmpty()) {
            logger.debug("Sender email is null or empty");
            return false;
        }
        
        String senderLower = senderEmail.toLowerCase();
        
            
        for (Platform platform : userPlatforms) {
            String platformName = platform.getPlatformName();
            if (platformName != null && senderLower.contains(platformName.toLowerCase())) {
                logger.debug("Email from {} matches platform {}", senderEmail, platformName);
                return true;
            }
        }
        
        logger.info("Email from {} SKIPPED - does not match any user's configured platforms (user has {} platforms)", 
            senderEmail, userPlatforms.size());
        return false;
    }
    
    private Platform findMatchingPlatform(String senderEmail, List<Platform> userPlatforms) {
        if (senderEmail == null || senderEmail.isEmpty() || userPlatforms.isEmpty()) {
            logger.debug("Cannot find matching platform - sender email or platforms list is empty");
            return null;
        }
        
        String senderLower = senderEmail.toLowerCase();
        
        
        for (Platform platform : userPlatforms) {
            String platformName = platform.getPlatformName();
            if (platformName != null && senderLower.contains(platformName.toLowerCase())) {
                logger.debug("Found matching platform: {} for sender: {}", platformName, senderEmail);
                return platform;
            }
        }
        
        logger.debug("No matching platform found for sender: {}", senderEmail);
        return null;
    }
    
    private void updateLastProcessedEmailTimestamp(User user) {
        try {
            if (user.getUserConfig() != null) {
                user.getUserConfig().setLastProcessedEmailTime(LocalDateTime.now());
                userConfigRepository.save(user.getUserConfig());
                logger.debug("Updated last processed email timestamp for user: {}", user.getUserId());
            }
        } catch (Exception e) {
            logger.warn("Failed to update last processed email timestamp for user {}: {}", user.getUserId(), e.getMessage());
        }
    }
    
    private void handleOrderCreationOrUpdate(User user, GeminiEmailAnalysisService.EmailAnalysisResult analysis, Platform platform) {
        try {
            if (analysis.getOrderId() == null) {
                logger.warn("Order ID is null in analysis result, skipping order creation for user: {}", user.getUserId());
                return;
            }
            
            logger.info("Processing order: {} for user: {} (isNewOrder: {})", 
                analysis.getOrderId(), user.getUserId(), analysis.isNewOrder());
            
            
            Optional<Order> existingOrder = orderRepository.findByOrderId(analysis.getOrderId());
            
            if (existingOrder.isPresent()) {
                logger.info("Order {} already exists, updating it", analysis.getOrderId());
                
                
                Order order = existingOrder.get();
                updateOrder(order, analysis, user, platform);
                Order savedOrder = orderRepository.save(order);
                
                logger.info("Successfully updated order: {} for user: {}", savedOrder.getId(), user.getUserId());
                
                
                try {
                    NotificationTemplates.NotificationTemplate template = 
                        notificationTemplateService.getTemplate(analysis.getShipmentStatus(), analysis.getOrderId(), analysis.getProductName());
                    notificationService.sendNotification(user.getUserId(), template.getTitle(), template.getBody());
                    logger.info("Sent templated notification for order update: {}", analysis.getOrderId());
                } catch (Exception notifException) {
                    logger.warn("Failed to send notification for order update {}: {}", analysis.getOrderId(), notifException.getMessage());
                }
                
            } else if (analysis.isNewOrder()) {
                logger.info("Creating new order: {} for user: {}", analysis.getOrderId(), user.getUserId());
                
                
                Order newOrder = createNewOrder(user, analysis, platform);
                Order savedOrder = orderRepository.save(newOrder);
                
                logger.info("Successfully created new order with ID: {} for user: {}", savedOrder.getId(), user.getUserId());
                
                
                try {
                    NotificationTemplates.NotificationTemplate template = 
                        notificationTemplateService.getTemplate(analysis.getShipmentStatus() != null ? analysis.getShipmentStatus() : "ordered", 
                            analysis.getOrderId(), analysis.getProductName());
                    notificationService.sendNotification(user.getUserId(), template.getTitle(), template.getBody());
                    logger.info("Sent templated notification for new order: {}", analysis.getOrderId());
                } catch (Exception notifException) {
                    logger.warn("Failed to send notification for new order {}: {}", analysis.getOrderId(), notifException.getMessage());
                }
            } else {
                
                logger.info("Order not found but has valid order ID, creating new order: {} for user: {}", analysis.getOrderId(), user.getUserId());
                
                
                Order newOrder = createNewOrder(user, analysis, platform);
                Order savedOrder = orderRepository.save(newOrder);
                
                logger.info("Successfully created order from email data: {} for user: {}", savedOrder.getId(), user.getUserId());
                
                
                try {
                    NotificationTemplates.NotificationTemplate template = 
                        notificationTemplateService.getTemplate(analysis.getShipmentStatus() != null ? analysis.getShipmentStatus() : "ordered", 
                            analysis.getOrderId(), analysis.getProductName());
                    notificationService.sendNotification(user.getUserId(), template.getTitle(), template.getBody());
                    logger.info("Sent templated notification for order created from email: {}", analysis.getOrderId());
                } catch (Exception notifException) {
                    logger.warn("Failed to send notification for order {}: {}", analysis.getOrderId(), notifException.getMessage());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error handling order creation/update for order {} and user {}: {}", 
                analysis.getOrderId(), user.getUserId(), e.getMessage(), e);
        }
    }
    
    private Order createNewOrder(User user, GeminiEmailAnalysisService.EmailAnalysisResult analysis, Platform platform) {
        logger.debug("Creating new order with ID: {} for user: {}", analysis.getOrderId(), user.getUserId());
        
        Order order = new Order();
        order.setUser(user);
        order.setOrderId(analysis.getOrderId());
        order.setProductLink(analysis.getProductLink());
        order.setQuantity(analysis.getQuantity() != null ? analysis.getQuantity() : 1);
        
        
        if (platform != null) {
            try {
                Long platformId = platform.getId();
                if (platformId != null) {
                    Platform refreshedPlatform = platformRepository.findById(platformId).orElse(null);
                    if (refreshedPlatform != null) {
                        order.setPlatform(refreshedPlatform);
                        logger.debug("Refreshed platform entity for order: {} with platform ID: {}", analysis.getOrderId(), platformId);
                    } else {
                        logger.warn("Platform with ID {} not found, order will not have platform assigned", platformId);
                    }
                } else {
                    logger.warn("Platform ID is null, cannot refresh platform for order: {}", analysis.getOrderId());
                }
            } catch (Exception e) {
                logger.warn("Error refreshing platform for order {}: {}", analysis.getOrderId(), e.getMessage());
            }
        } else {
            logger.debug("Platform is null for order: {}, skipping platform assignment", analysis.getOrderId());
        }
        
        
        if (analysis.getCategoryMatches() != null && !analysis.getCategoryMatches().isEmpty()) {
            try {
                String categoryName = analysis.getCategoryMatches().get(0);
                List<Category> userCategories = categoryRepository.findByUserAndIsDeletedFalse(user);
                Category matchedCategory = userCategories.stream()
                    .filter(cat -> cat.getCategoryName() != null && cat.getCategoryName().equalsIgnoreCase(categoryName))
                    .findFirst()
                    .orElse(null);
                if (matchedCategory != null) {
                    order.setCategory(matchedCategory);
                    logger.debug("Set category '{}' for order: {}", categoryName, analysis.getOrderId());
                } else {
                    logger.warn("Category '{}' not found for user, skipping category assignment", categoryName);
                }
            } catch (Exception e) {
                logger.warn("Error setting category for order {}: {}", analysis.getOrderId(), e.getMessage());
            }
        }
        
        
        if (analysis.getVendor() != null && !analysis.getVendor().isEmpty()) {
            try {
                Vendor vendor = vendorRepository.findByVendorName(analysis.getVendor()).orElse(null);
                if (vendor != null) {
                    order.setVendor(vendor);
                    logger.debug("Set vendor '{}' for order: {}", analysis.getVendor(), analysis.getOrderId());
                } else {
                    logger.debug("Vendor '{}' not found, will not assign vendor to order: {}", analysis.getVendor(), analysis.getOrderId());
                }
            } catch (Exception e) {
                logger.debug("Error finding vendor '{}' for order {}: {}", analysis.getVendor(), analysis.getOrderId(), e.getMessage());
            }
        }
        
        if (analysis.getPrice() != null) {
            order.setPrice(BigDecimal.valueOf(analysis.getPrice()));
            logger.debug("Order price: {}", analysis.getPrice());
        } else {
            order.setPrice(BigDecimal.ZERO);
        }
        
        LocalDateTime orderDate = parseDateTime(analysis.getOrderDate());
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
            logger.debug("Order date was null, using current timestamp: {}", orderDate);
        }
        order.setOrderDate(orderDate);
        order.setDeliveryDate(parseDateTime(analysis.getDeliveryDate()));
        order.setShipmentStatus(analysis.getShipmentStatus());
        order.setCreateUser(user.getUserId());
        order.setUpdateUser(user.getUserId());
        
        logger.info("New Order prepared: orderId={}, price={}, quantity={}, status={}, orderDate={}, deliveryDate={}, productLink={}, platform={}, category={}, vendor={}", 
            order.getOrderId(), order.getPrice(), order.getQuantity(), order.getShipmentStatus(), 
            order.getOrderDate(), order.getDeliveryDate(), order.getProductLink(), 
            platform != null ? platform.getPlatformName() : "None",
            order.getCategory() != null ? order.getCategory().getCategoryName() : "None",
            order.getVendor() != null ? order.getVendor().getVendorName() : "None");
        
        return order;
    }
    
    private void updateOrder(Order order, GeminiEmailAnalysisService.EmailAnalysisResult analysis, User user, Platform platform) {
        logger.debug("Updating order: {} for user: {}", order.getOrderId(), user.getUserId());
        
        StringBuilder updateLog = new StringBuilder("Order update fields: ");
        
        if (analysis.getPrice() != null) {
            order.setPrice(BigDecimal.valueOf(analysis.getPrice()));
            updateLog.append("price=").append(analysis.getPrice()).append(" ");
        }
        
        if (analysis.getQuantity() != null) {
            order.setQuantity(analysis.getQuantity());
            updateLog.append("quantity=").append(analysis.getQuantity()).append(" ");
        }
        
        if (analysis.getProductLink() != null) {
            order.setProductLink(analysis.getProductLink());
            updateLog.append("productLink=present ");
        }
        
        if (analysis.getDeliveryDate() != null) {
            order.setDeliveryDate(parseDateTime(analysis.getDeliveryDate()));
            updateLog.append("deliveryDate=").append(analysis.getDeliveryDate()).append(" ");
        }
        
        if (analysis.getShipmentStatus() != null) {
            String oldStatus = order.getShipmentStatus();
            order.setShipmentStatus(analysis.getShipmentStatus());
            updateLog.append("status=").append(oldStatus).append("->").append(analysis.getShipmentStatus()).append(" ");
        }
        
        if (platform != null && order.getPlatform() == null) {
            // Refresh platform in current transaction to avoid detached entity error
            try {
                Long platformId = platform.getId();
                if (platformId != null) {
                    Platform refreshedPlatform = platformRepository.findById(platformId).orElse(null);
                    if (refreshedPlatform != null) {
                        order.setPlatform(refreshedPlatform);
                        updateLog.append("platform=").append(platform.getPlatformName()).append(" ");
                        logger.debug("Refreshed platform entity for order: {}", order.getOrderId());
                    }
                }
            } catch (Exception e) {
                logger.warn("Error refreshing platform for order {}: {}", order.getOrderId(), e.getMessage());
            }
        }
        
        order.setUpdateUser(user.getUserId());
        
        logger.info("{}", updateLog.toString());
    }
    
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            logger.debug("DateTime string is null or empty");
            return null;
        }
        
        try {

            LocalDateTime parsed = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);
            logger.debug("Successfully parsed datetime: {} -> {}", dateTimeStr, parsed);
            return parsed;
        } catch (Exception e1) {
            try {
                LocalDateTime parsed = LocalDateTime.parse(dateTimeStr.replace("Z", ""), 
                    DateTimeFormatter.ISO_DATE_TIME);
                logger.debug("Successfully parsed datetime (alternative format): {} -> {}", dateTimeStr, parsed);
                return parsed;
            } catch (Exception e2) {
                logger.warn("Could not parse datetime: {} (error: {})", dateTimeStr, e2.getMessage());
                return null;
            }
        }
    }
    
    private boolean hasValidAccessToken(User user) {
        return user != null && 
               user.getAccessToken() != null && 
               !user.getAccessToken().equals("gmail_access_granted") &&
               !user.getAccessToken().trim().isEmpty();
    }
}
