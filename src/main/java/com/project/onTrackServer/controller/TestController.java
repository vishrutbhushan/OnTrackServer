package com.project.onTrackServer.controller;

import com.project.onTrackServer.model.Vendor;
import com.project.onTrackServer.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestController {
    
    @Autowired
    private VendorRepository vendorRepository;
    
    @GetMapping("/vendors")
    public ResponseEntity<List<Vendor>> getAllVendors() {
        try {
            List<Vendor> vendors = vendorRepository.findAll();
            return ResponseEntity.ok(vendors);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/vendor")
    public ResponseEntity<Vendor> createVendor(@RequestBody Vendor vendor) {
        try {
            Vendor savedVendor = vendorRepository.save(vendor);
            return ResponseEntity.ok(savedVendor);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Test controller is working!");
    }
    
    // Basic CRUD operations for all entities
    @Autowired
    private com.project.onTrackServer.repository.LogisticProviderRepository logisticProviderRepository;
    
    @Autowired 
    private com.project.onTrackServer.repository.EcommercePlatformRepository ecommercePlatformRepository;
    
    @Autowired
    private com.project.onTrackServer.repository.OrdersRepository ordersRepository;
    
    @GetMapping("/logistics")
    public ResponseEntity<List<com.project.onTrackServer.model.LogisticProvider>> getAllLogistics() {
        try {
            return ResponseEntity.ok(logisticProviderRepository.findAll());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/logistic")
    public ResponseEntity<com.project.onTrackServer.model.LogisticProvider> createLogistic(@RequestBody com.project.onTrackServer.model.LogisticProvider provider) {
        try {
            return ResponseEntity.ok(logisticProviderRepository.save(provider));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/platforms")
    public ResponseEntity<List<com.project.onTrackServer.model.EcommercePlatform>> getAllPlatforms() {
        try {
            return ResponseEntity.ok(ecommercePlatformRepository.findAll());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/platform")
    public ResponseEntity<com.project.onTrackServer.model.EcommercePlatform> createPlatform(@RequestBody com.project.onTrackServer.model.EcommercePlatform platform) {
        try {
            return ResponseEntity.ok(ecommercePlatformRepository.save(platform));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/orders")
    public ResponseEntity<List<com.project.onTrackServer.model.Orders>> getAllOrders() {
        try {
            return ResponseEntity.ok(ordersRepository.findAll());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PostMapping("/order")
    public ResponseEntity<com.project.onTrackServer.model.Orders> createOrder(@RequestBody com.project.onTrackServer.model.Orders order) {
        try {
            return ResponseEntity.ok(ordersRepository.save(order));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
