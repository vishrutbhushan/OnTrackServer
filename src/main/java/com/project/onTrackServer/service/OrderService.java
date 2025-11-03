package com.project.onTrackServer.service;

import com.project.onTrackServer.model.Order;
import com.project.onTrackServer.model.User;
import com.project.onTrackServer.repository.OrderRepository;
import com.project.onTrackServer.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class OrderService {
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * CONTRACT: userId parameter MUST be the email address.
     * This is the only identifier used in API communication.
     */
    public Order createOrder(String userId, Order orderData) {
        log.info("Creating order: {} for user: {}", orderData.getOrderId(), userId);
        
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        orderData.setUser(user);
        orderData.setCreateUser(userId);
        orderData.setUpdateUser(userId);
        
        // Cascade will handle saving platform, category, vendor, logistic provider if they are new
        return orderRepository.save(orderData);
    }
    
    /**
     * CONTRACT: userId parameter MUST be the email address.
     */
    public Optional<Order> getOrder(Long orderId, String userId) {
        log.info("Fetching order: {} for user: {}", orderId, userId);
        
        return userRepository.findByUserId(userId)
                .flatMap(user -> orderRepository.findByIdAndUser(orderId, user));
    }
    
    public Optional<Order> getOrderByOrderId(String orderId) {
        log.info("Fetching order by order ID: {}", orderId);
        return orderRepository.findByOrderId(orderId);
    }
    
    /**
     * CONTRACT: userId parameter MUST be the email address.
     */
    public List<Order> getUserOrders(String userId) {
        log.info("Fetching all orders for user: {}", userId);
        
        return userRepository.findByUserId(userId)
                .map(user -> orderRepository.findByUserAndIsDeletedFalse(user))
                .orElse(List.of());
    }
    
    /**
     * CONTRACT: userId parameter MUST be the email address.
     */
    public Order updateOrder(Long orderId, String userId, Order orderData) {
        log.info("Updating order: {} for user: {}", orderId, userId);
        
        Order order = getOrder(orderId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        
        order.setPrice(orderData.getPrice());
        order.setQuantity(orderData.getQuantity());
        order.setProductLink(orderData.getProductLink());
        order.setDeliveryDate(orderData.getDeliveryDate());
        order.setRating(orderData.getRating());
        order.setShipmentStatus(orderData.getShipmentStatus());
        order.setUpdateUser(userId);
        
        return orderRepository.save(order);
    }
    
    public Order updateOrderStatus(Long orderId, String userId, String status) {
        log.info("Updating order status: {} to {} for user: {}", orderId, status, userId);
        
        Order order = getOrder(orderId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        
        order.setShipmentStatus(status);
        order.setUpdateUser(userId);
        
        return orderRepository.save(order);
    }
    
    public Order updateOrderRating(Long orderId, String userId, Double rating) {
        log.info("Updating order rating: {} for user: {}", orderId, userId);
        
        Order order = getOrder(orderId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        
        order.setRating(rating);
        order.setUpdateUser(userId);
        
        return orderRepository.save(order);
    }
    
    public void deleteOrder(Long orderId, String userId) {
        log.info("Deleting order: {} for user: {}", orderId, userId);
        
        Order order = getOrder(orderId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        
        order.setIsDeleted(true);
        order.setUpdateUser(userId);
        orderRepository.save(order);
    }
}
