package com.project.onTrackServer.controller;

import java.math.BigDecimal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.onTrackServer.Models.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")

public class FacadeController {

        @PostMapping("/entity")
        public ResponseEntity<String> entityAction(@RequestParam String entity,
            @RequestParam String action,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long entityId,
            @RequestParam(required = false) String name) throws Exception {
        BaseEntity entityObj;
        User user = User.findByUserId(userId.toString());
        if (user == null)
            return ResponseEntity.ok("Not found");
        entityObj = EntityFactory.createEntity(entity);

        switch (action) {
            case "create":
                return ResponseEntity.ok(entityObj.create(user, name).toString());
            case "list":
                return ResponseEntity.ok(entityObj.findByUser(user).toString());
            case "delete":
                boolean deleted = entityObj.delete(user, entityId);
                return ResponseEntity.ok(deleted ? (entity + " deleted") : "Not found");
            default:
                return ResponseEntity.badRequest().body("Unknown action");
        }
    }

        @PostMapping("/user")
        public ResponseEntity<String> userAction(@RequestParam(required = false) Long userId,
            @RequestParam(required = false) String accessToken,
            @RequestParam(required = false) String fcmToken,
            @RequestParam(required = false) User user,
            @RequestParam String action) throws Exception {
        switch (action) {
            case "create":
                return ResponseEntity.ok(User.create(user).toString());
            case "get":
                if (userId == null)
                    return ResponseEntity.badRequest().body("userId required");
                User foundUser = User.findByUserId(userId.toString());
                if (foundUser == null)
                    return ResponseEntity.ok("Not found");
                return ResponseEntity.ok(foundUser.toString());
            case "update":
                if (user == null || userId == null)
                    return ResponseEntity.badRequest().body("user and userId required");
                user.setUserId(userId.toString());
                return ResponseEntity.ok(User.update(user).toString());
            case "updateAccessToken":
                if (userId == null || accessToken == null)
                    return ResponseEntity.badRequest().body("userId and accessToken required");
                return ResponseEntity.ok(User.updateAccessToken(userId.toString(), accessToken).toString());
            case "updateFcmToken":
                if (userId == null || fcmToken == null)
                    return ResponseEntity.badRequest().body("userId and fcmToken required");
                return ResponseEntity.ok(User.updateFcmToken(userId.toString(), fcmToken).toString());
            default:
                return ResponseEntity.badRequest().body("Unknown action");
        }
    }

        @PostMapping("/dashboard")
        public ResponseEntity<String> dashboardAction(@RequestParam(required = false) Long userId,
            @RequestParam String action) throws Exception {

        User user = User.findByUserId(userId.toString());
        if (user == null)
            return ResponseEntity.ok("Not found");

        switch (action) {
            case "kpi":
                long totalOrders = Order.findByUserAndIsDeletedFalse(user).size();
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
    }
}
