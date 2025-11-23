package com.project.onTrackServer.controller;
import com.project.onTrackServer.model.*;
import java.math.BigDecimal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")

public class FacadeController {

    @PostMapping("/entity")
    public ResponseEntity<String> entityAction(@RequestParam String entity,
            @RequestParam String action,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) String name) {
        try {
            BaseEntity entityObj;
            try {
                entityObj = EntityFactory.createEntity(entity);
            } catch (IllegalArgumentException ex) {
                return ResponseEntity.badRequest().body("Unknown entity");
            }

            switch (action) {
                case "create":
                    return ResponseEntity.ok(entityObj.create(userId, name).toString());
                case "list":
                    return ResponseEntity.ok(entityObj.findByUser(userId).toString());
                case "delete":
                    boolean deleted = entityObj.delete(userId, entityId);
                    return ResponseEntity.ok(deleted ? (entity + " deleted") : "Not found");
                default:
                    return ResponseEntity.badRequest().body("Unknown action");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping("/user")
    public ResponseEntity<String> userAction(@RequestParam(required = false) Long userId,
            @RequestParam(required = false) String accessToken,
            @RequestParam(required = false) String fcmToken,
            @RequestParam(required = false) User user,
            @RequestParam String action) {
        try {

            User userObj = new User();

            switch (action) {
                case "create":
                    return ResponseEntity.ok(userObj.create(user).toString());
                case "get":
                    User foundUser = user.findByUserId(userObj.toString());
                    if (foundUser == null)
                        return ResponseEntity.ok("Not found");
                    return ResponseEntity.ok(foundUser.toString());
                case "update":
                    user.setUserId(userId.toString());
                    return ResponseEntity.ok(userObj.update(user).toString());
                case "updateAccessToken":
                    return ResponseEntity.ok(userObj.updateAccessToken(userId.toString(), accessToken).toString());
                case "updateFcmToken":
                    return ResponseEntity.ok(userObj.updateFcmToken(userId.toString(), fcmToken).toString());
                default:
                    return ResponseEntity.badRequest().body("Unknown action");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping("/dashboard")
    public ResponseEntity<String> dashboardAction(@RequestParam(required = false) Long userId,
            @RequestParam String action) {
        try {
            switch (action) {
                case "kpi":
                    long totalOrders = Order.findByUserAndIsDeletedFalse(userId).size();
                    BigDecimal totalSpent = Order.getTotalSpent(userId);
                    BigDecimal last30DaysSpent = Order.getTotalSpentLast30Days(userId,
                            java.time.LocalDateTime.now().minusDays(30));
                    long activeOrders = Order.countActiveOrders(userId);
                    return ResponseEntity
                            .ok(totalOrders + " " + totalSpent + " " + last30DaysSpent + " " + activeOrders);
                case "monthlySpending":
                    return ResponseEntity.ok(Order
                            .getMonthlySpendData(userId, java.time.LocalDateTime.now().minusMonths(12)).toString());
                case "categorySpending":
                    return ResponseEntity.ok(Order.getSpendingByCategory(userId).toString());
                case "platformDistribution":
                    return ResponseEntity.ok(Order.getSpendingByPlatform(userId).toString());
                case "activeOrders":
                    return ResponseEntity.ok(Order.getActiveOrders(userId).toString());
                default:
                    return ResponseEntity.badRequest().body("Unknown action");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}
