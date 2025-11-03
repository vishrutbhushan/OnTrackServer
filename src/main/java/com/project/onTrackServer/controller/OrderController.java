package com.project.onTrackServer.controller;

import com.project.onTrackServer.dto.ApiResponse;
import com.project.onTrackServer.model.Order;
import com.project.onTrackServer.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
@Slf4j
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    @PostMapping("/{userId}")
    public ResponseEntity<Order> createOrder(
            @PathVariable String userId,
            @RequestBody Order order) {
        log.info("Create order request for user: {}", userId);
        Order created = orderService.createOrder(userId, order);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping("/{userId}/{orderId}")
    public ResponseEntity<Order> getOrder(
            @PathVariable String userId,
            @PathVariable Long orderId) {
        log.info("Get order: {} for user: {}", orderId, userId);
        return orderService.getOrder(orderId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/order-id/{orderId}")
    public ResponseEntity<Order> getOrderByOrderId(@PathVariable String orderId) {
        log.info("Get order by order ID: {}", orderId);
        return orderService.getOrderByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{userId}")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable String userId) {
        log.info("Get all orders for user: {}", userId);
        List<Order> orders = orderService.getUserOrders(userId);
        return ResponseEntity.ok(orders);
    }
    
    @PutMapping("/{userId}/{orderId}")
    public ResponseEntity<Order> updateOrder(
            @PathVariable String userId,
            @PathVariable Long orderId,
            @RequestBody Order order) {
        log.info("Update order: {} for user: {}", orderId, userId);
        Order updated = orderService.updateOrder(orderId, userId, order);
        return ResponseEntity.ok(updated);
    }
    
    @PutMapping("/{userId}/{orderId}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable String userId,
            @PathVariable Long orderId,
            @RequestBody Map<String, String> request) {
        log.info("Update order status: {} for user: {}", orderId, userId);
        String status = request.get("status");
        Order updated = orderService.updateOrderStatus(orderId, userId, status);
        return ResponseEntity.ok(updated);
    }
    
    @PutMapping("/{userId}/{orderId}/rating")
    public ResponseEntity<Order> updateOrderRating(
            @PathVariable String userId,
            @PathVariable Long orderId,
            @RequestBody Map<String, Double> request) {
        log.info("Update order rating: {} for user: {}", orderId, userId);
        Double rating = request.get("rating");
        Order updated = orderService.updateOrderRating(orderId, userId, rating);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{userId}/{orderId}")
    public ResponseEntity<ApiResponse> deleteOrder(
            @PathVariable String userId,
            @PathVariable Long orderId) {
        log.info("Delete order: {} for user: {}", orderId, userId);
        orderService.deleteOrder(orderId, userId);
        return ResponseEntity.ok(new ApiResponse(true, "Order deleted successfully"));
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgument(IllegalArgumentException e) {
        log.error("Error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(false, e.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(Exception e) {
        log.error("Unexpected error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse(false, "Internal server error: " + e.getMessage()));
    }
}
