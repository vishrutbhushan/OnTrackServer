package com.project.onTrackServer.service;

import com.project.onTrackServer.model.Item;
import com.project.onTrackServer.model.User;
import com.project.onTrackServer.model.Order;
import com.project.onTrackServer.model.Platform;
import com.project.onTrackServer.model.Category;
import com.project.onTrackServer.repository.ItemRepository;
import com.project.onTrackServer.repository.UserRepository;
import com.project.onTrackServer.repository.OrderRepository;
import com.project.onTrackServer.repository.UserConfigRepository;
import com.project.onTrackServer.repository.PlatformRepository;
import com.project.onTrackServer.repository.CategoryRepository;
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
import java.util.Map;
import java.math.BigDecimal;

@Service
@ConditionalOnProperty(name = "email.processing.schedule.enabled", havingValue = "true", matchIfMissing = true)
public class EmailProcessingSchedulerService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailProcessingSchedulerService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ItemRepository itemRepository;
    
    @Autowired
    private UserConfigRepository userConfigRepository;
    
    @Autowired
    private PlatformRepository platformRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private GmailService gmailService;
    
    @Autowired
    private GeminiEmailAnalysisService emailAnalysisService;
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Value("${email.processing.schedule.interval:10}")
    private int intervalMinutes;
    
    @Scheduled(fixedDelayString = "#{${email.processing.schedule.interval:10} * 60 * 1000}") // Convert minutes to milliseconds
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
            
            // Get user's platforms
            List<Platform> userPlatforms = platformRepository.findByUserAndIsDeletedFalse(user);
            logger.debug("User has {} platforms", userPlatforms.size());
            
            // Get user's categories
            List<Category> userCategories = categoryRepository.findByUserAndIsDeletedFalse(user);
            List<String> categoryNames = userCategories.stream()
                .map(Category::getCategoryName)
                .toList();
            logger.debug("User has {} categories: {}", userCategories.size(), categoryNames);
            
            List<Item> newEmails = gmailService.fetchNewEmailsFromGmail(user, itemRepository);
            
            if (!newEmails.isEmpty()) {
                for (Item email : newEmails) {
                    // Filter email by platform if user has defined platforms
                    if (!userPlatforms.isEmpty() && !isPlatformAllowed(email.getSender(), userPlatforms)) {
                        logger.info("Email from {} skipped - not in user's allowed platforms", email.getSender());
                        continue;
                    }
                    
                    processAndSaveEmail(email, user, categoryNames);
                    // Update the user config with the last processed email info
                    if (email.getGmailMessageId() != null) {
                        updateLastProcessedEmail(user, email.getGmailMessageId());
                    }
                }
                
            } else {
                logger.debug("No new emails found for user: {}", user.getUserId());
            }
            
        } catch (Exception e) {
            logger.error("Error processing emails for user {}: {}", user.getUserId(), e.getMessage());
        }
    }
    
    private boolean isPlatformAllowed(String senderEmail, List<Platform> userPlatforms) {
        if (senderEmail == null || senderEmail.isEmpty()) {
            logger.debug("Sender email is null or empty");
            return false;
        }
        
        String senderLower = senderEmail.toLowerCase();
        
        // Map of common platform email domains
        Map<String, List<String>> platformEmailDomains = new java.util.HashMap<>();
        platformEmailDomains.put("Amazon", List.of("@amazon.com", "@amazon.in", "@amazonses.com"));
        platformEmailDomains.put("Flipkart", List.of("@flipkart.com", "@fkart.com"));
        platformEmailDomains.put("eBay", List.of("@ebay.com"));
        platformEmailDomains.put("Walmart", List.of("@walmart.com"));
        platformEmailDomains.put("Target", List.of("@target.com"));
        platformEmailDomains.put("Best Buy", List.of("@bestbuy.com"));
        platformEmailDomains.put("Shopify", List.of("@shopify.com"));
        platformEmailDomains.put("Etsy", List.of("@etsy.com"));
        platformEmailDomains.put("AliExpress", List.of("@aliexpress.com"));
        
        for (Platform platform : userPlatforms) {
            String platformName = platform.getPlatformName();
            List<String> domains = platformEmailDomains.getOrDefault(platformName, List.of());
            
            for (String domain : domains) {
                if (senderLower.contains(domain)) {
                    logger.debug("Email from {} matches platform {}", senderEmail, platformName);
                    return true;
                }
            }
        }
        
        logger.info("Email from {} SKIPPED - does not match any user's configured platforms (user has {} platforms)", 
            senderEmail, userPlatforms.size());
        return false;
    }
    
    private void updateLastProcessedEmail(User user, String messageId) {
        try {
            if (user.getUserConfig() != null) {
                user.getUserConfig().setLastProcessedEmailId(messageId);
                user.getUserConfig().setLastProcessedEmailTime(LocalDateTime.now());
                userConfigRepository.save(user.getUserConfig());
                logger.debug("Updated last processed email for user: {} with message ID: {}", user.getUserId(), messageId);
            }
        } catch (Exception e) {
            logger.warn("Failed to update last processed email info for user {}: {}", user.getUserId(), e.getMessage());
        }
    }
    
    private void processAndSaveEmail(Item email, User user, List<String> userCategories) {
        try {
            email.setUserId(user.getUserId());
            
            logger.info("Processing email from sender: {} with subject: {} for user: {}", 
                email.getSender(), email.getSubject(), user.getUserId());
            
            // Analyze email with Gemini AI
            logger.debug("Sending email to Gemini for analysis - Subject: {}, Sender: {}", 
                email.getSubject(), email.getSender());
            
            GeminiEmailAnalysisService.EmailAnalysisResult analysis = 
                emailAnalysisService.analyzeEmail(email.getBody() != null ? email.getBody() : email.getSnippet(), 
                    email.getSubject(), email.getSender(), userCategories);
            
            logger.info("Gemini analysis result - isOrderRelated: {}, orderId: {}, isNewOrder: {}, shipmentStatus: {}", 
                analysis.isOrderRelatedEmail(), analysis.getOrderId(), analysis.isNewOrder(), analysis.getShipmentStatus());
            
            // Only process order-related emails
            if (analysis.isOrderRelatedEmail()) {
                logger.info("Email identified as order-related");
                
                // Extract platform name from sender email
                String platformName = extractPlatformName(email.getSender());
                logger.debug("Extracted platform name: {}", platformName);
                
                // Create or update order (do NOT save to Item table)
                handleOrderCreationOrUpdate(user, analysis, platformName);
                
                // Archive email if auto-archive is enabled in user config
                if (user.getUserConfig() != null && 
                    user.getUserConfig().getAutoArchiveOrderEmails() != null &&
                    user.getUserConfig().getAutoArchiveOrderEmails()) {
                    
                    logger.debug("Auto-archive is enabled for user: {}", user.getUserId());
                    
                    if (email.getGmailMessageId() != null) {
                        gmailService.archiveEmail(user, email.getGmailMessageId());
                        logger.info("Archived order email for user: {} with message ID: {}", 
                            user.getUserId(), email.getGmailMessageId());
                    } else {
                        logger.warn("Gmail message ID is null, cannot archive email for user: {}", user.getUserId());
                    }
                } else {
                    logger.debug("Auto-archive is disabled for user: {}", user.getUserId());
                }
                
                // Update the user config with the last processed email info
                if (email.getGmailMessageId() != null) {
                    updateLastProcessedEmail(user, email.getGmailMessageId());
                }
                
            } else {
                logger.info("Email skipped - NOT order-related from sender: {}", email.getSender());
            }
            
        } catch (Exception e) {
            logger.error("Error processing email from sender {} for user {}: {}", 
                email.getSender(), user.getUserId(), e.getMessage(), e);
        }
    }
    
    private void handleOrderCreationOrUpdate(User user, GeminiEmailAnalysisService.EmailAnalysisResult analysis, String platformName) {
        try {
            if (analysis.getOrderId() == null) {
                logger.warn("Order ID is null in analysis result, skipping order creation for user: {}", user.getUserId());
                return;
            }
            
            logger.info("Processing order: {} for user: {} (isNewOrder: {})", 
                analysis.getOrderId(), user.getUserId(), analysis.isNewOrder());
            
            // Check if order already exists
            Optional<Order> existingOrder = orderRepository.findByOrderId(analysis.getOrderId());
            
            if (existingOrder.isPresent()) {
                logger.info("Order {} already exists, updating it", analysis.getOrderId());
                
                // Update existing order
                Order order = existingOrder.get();
                updateOrder(order, analysis, user);
                Order savedOrder = orderRepository.save(order);
                
                logger.info("Successfully updated order: {} for user: {}", savedOrder.getId(), user.getUserId());
                
                String message = "Order " + analysis.getOrderId() + " updated - Status: " + analysis.getShipmentStatus();
                try {
                    notificationService.sendNotification(user.getUserId(), "Order Update", message);
                } catch (Exception notifException) {
                    logger.warn("Failed to send notification for order update {}: {}", analysis.getOrderId(), notifException.getMessage());
                }
                
            } else if (analysis.isNewOrder()) {
                logger.info("Creating new order: {} for user: {}", analysis.getOrderId(), user.getUserId());
                
                // Create new order
                Order newOrder = createNewOrder(user, analysis, platformName);
                Order savedOrder = orderRepository.save(newOrder);
                
                logger.info("Successfully created new order with ID: {} for user: {}", savedOrder.getId(), user.getUserId());
                
                String message = "New order confirmed - Order ID: " + analysis.getOrderId();
                try {
                    notificationService.sendNotification(user.getUserId(), "Order Confirmed", message);
                } catch (Exception notifException) {
                    logger.warn("Failed to send notification for new order {}: {}", analysis.getOrderId(), notifException.getMessage());
                }
            } else {
                // Order doesn't exist and isNewOrder=false, but we have an order ID so create it
                logger.info("Order not found but has valid order ID, creating new order: {} for user: {}", analysis.getOrderId(), user.getUserId());
                
                // Create new order from email data
                Order newOrder = createNewOrder(user, analysis, platformName);
                Order savedOrder = orderRepository.save(newOrder);
                
                logger.info("Successfully created order from email data: {} for user: {}", savedOrder.getId(), user.getUserId());
                
                String message = "Order " + analysis.getOrderId() + " created from email - Status: " + analysis.getShipmentStatus();
                try {
                    notificationService.sendNotification(user.getUserId(), "Order Added", message);
                } catch (Exception notifException) {
                    logger.warn("Failed to send notification for order {}: {}", analysis.getOrderId(), notifException.getMessage());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error handling order creation/update for order {} and user {}: {}", 
                analysis.getOrderId(), user.getUserId(), e.getMessage(), e);
        }
    }
    
    private Order createNewOrder(User user, GeminiEmailAnalysisService.EmailAnalysisResult analysis, String platformName) {
        logger.debug("Creating new order with ID: {} for user: {}", analysis.getOrderId(), user.getUserId());
        
        Order order = new Order();
        order.setUser(user);
        order.setOrderId(analysis.getOrderId());
        order.setProductLink(analysis.getProductLink());
        order.setQuantity(analysis.getQuantity() != null ? analysis.getQuantity() : 1);
        
        if (analysis.getPrice() != null) {
            order.setPrice(BigDecimal.valueOf(analysis.getPrice()));
            logger.debug("Order price: {}", analysis.getPrice());
        } else {
            // Set default price to 0 if not provided
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
        
        logger.info("New Order prepared: orderId={}, price={}, quantity={}, status={}, orderDate={}, deliveryDate={}, productLink={}", 
            order.getOrderId(), order.getPrice(), order.getQuantity(), order.getShipmentStatus(), 
            order.getOrderDate(), order.getDeliveryDate(), order.getProductLink());
        
        return order;
    }
    
    private void updateOrder(Order order, GeminiEmailAnalysisService.EmailAnalysisResult analysis, User user) {
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
        
        order.setUpdateUser(user.getUserId());
        
        logger.info("{}", updateLog.toString());
    }
    
    private String extractPlatformName(String senderEmail) {
        if (senderEmail == null || senderEmail.isEmpty()) {
            return null;
        }
        
        senderEmail = senderEmail.toLowerCase();
        
        // Simple contains matching for platform names
        if (senderEmail.contains("amazon")) return "Amazon";
        if (senderEmail.contains("flipkart")) return "Flipkart";
        if (senderEmail.contains("ebay")) return "eBay";
        if (senderEmail.contains("walmart")) return "Walmart";
        if (senderEmail.contains("target")) return "Target";
        if (senderEmail.contains("bestbuy")) return "Best Buy";
        if (senderEmail.contains("shopify")) return "Shopify";
        if (senderEmail.contains("etsy")) return "Etsy";
        if (senderEmail.contains("aliexpress")) return "AliExpress";
        
        return null;
    }
    
    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            logger.debug("DateTime string is null or empty");
            return null;
        }
        
        try {
            // Try ISO 8601 format first
            LocalDateTime parsed = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);
            logger.debug("Successfully parsed datetime: {} -> {}", dateTimeStr, parsed);
            return parsed;
        } catch (Exception e1) {
            try {
                // Try alternative formats
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
