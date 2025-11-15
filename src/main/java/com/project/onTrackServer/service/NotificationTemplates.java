package com.project.onTrackServer.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for generating notification templates based on order status
 * Each status has a customized template with placeholders for dynamic data
 */
@Service
public class NotificationTemplates {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationTemplates.class);
    
    /**
     * Enum for all possible order statuses
     */
    public enum OrderStatus {
        ORDERED("ordered", "Order Placed"),
        SHIPPED("shipped", "Order Shipped"),
        OUT_OF_DELIVERY("out_of_delivery", "Out for Delivery"),
        DELIVERED("delivered", "Order Delivered"),
        CANCELLED("cancelled", "Order Cancelled");
        
        private final String code;
        private final String displayName;
        
        OrderStatus(String code, String displayName) {
            this.code = code;
            this.displayName = displayName;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public static OrderStatus fromCode(String code) {
            if (code == null) {
                return null;
            }
            for (OrderStatus status : OrderStatus.values()) {
                if (status.code.equalsIgnoreCase(code)) {
                    return status;
                }
            }
            return null;
        }
    }
    
    /**
     * Template data holder class
     */
    public static class NotificationTemplate {
        private final String title;
        private final String body;
        
        public NotificationTemplate(String title, String body) {
            this.title = title;
            this.body = body;
        }
        
        public String getTitle() {
            return title;
        }
        
        public String getBody() {
            return body;
        }
    }
    
    /**
     * Get notification template for a specific order status
     * @param status The order status
     * @param orderId The order ID
     * @param productName The product name
     * @return NotificationTemplate with title and body
     */
    public NotificationTemplate getTemplate(String status, String orderId, String productName) {
        OrderStatus orderStatus = OrderStatus.fromCode(status);
        
        if (orderStatus == null) {
            logger.warn("Unknown order status: {}, using default template", status);
            return getDefaultTemplate(orderId, productName);
        }
        
        logger.debug("Generating notification template for status: {}", orderStatus.getDisplayName());
        
        return switch (orderStatus) {
            case ORDERED -> getOrderedTemplate(orderId, productName);
            case SHIPPED -> getShippedTemplate(orderId, productName);
            case OUT_OF_DELIVERY -> getOutOfDeliveryTemplate(orderId, productName);
            case DELIVERED -> getDeliveredTemplate(orderId, productName);
            case CANCELLED -> getCancelledTemplate(orderId, productName);
        };
    }
    
    /**
     * Template: Order Placed
     */
    private NotificationTemplate getOrderedTemplate(String orderId, String productName) {
        String title = "Order Confirmed";
        String body = "Your order " + orderId + " has been confirmed!\n" +
                     "Product: " + (productName != null ? productName : "Your item");
        return new NotificationTemplate(title, body);
    }
    
    /**
     * Template: Order Shipped
     */
    private NotificationTemplate getShippedTemplate(String orderId, String productName) {
        String title = "Order Shipped";
        String body = "Your order " + orderId + " is on its way!\n" +
                     "Item: " + (productName != null ? productName : "Your item");
        return new NotificationTemplate(title, body);
    }
    
    /**
     * Template: Out for Delivery
     */
    private NotificationTemplate getOutOfDeliveryTemplate(String orderId, String productName) {
        String title = "Out for Delivery";
        String body = "Great news! Order " + orderId + " is out for delivery today.\n" +
                     "Item: " + (productName != null ? productName : "Your item");
        return new NotificationTemplate(title, body);
    }
    
    /**
     * Template: Order Delivered
     */
    private NotificationTemplate getDeliveredTemplate(String orderId, String productName) {
        String title = "Order Delivered";
        String body = "Your order " + orderId + " has been delivered!\n" +
                     "Item: " + (productName != null ? productName : "Your item");
        return new NotificationTemplate(title, body);
    }
    
    /**
     * Template: Order Cancelled
     */
    private NotificationTemplate getCancelledTemplate(String orderId, String productName) {
        String title = "Order Cancelled";
        String body = "Order " + orderId + " has been cancelled.\n" +
                     "Item: " + (productName != null ? productName : "Your item") ;
        return new NotificationTemplate(title, body);
    }
    
    /**
     * Default template for unknown status
     */
    private NotificationTemplate getDefaultTemplate(String orderId, String productName) {
        String title = "Order Update";
        String body = "Order " + orderId + " update:\n" +
                     "Item: " + (productName != null ? productName : "Your item") + "\n" +
                     "Check your order status anytime.";
        return new NotificationTemplate(title, body);
    }

}
