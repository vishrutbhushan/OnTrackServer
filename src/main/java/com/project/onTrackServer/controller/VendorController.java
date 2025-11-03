package com.project.onTrackServer.controller;

import com.project.onTrackServer.dto.ApiResponse;
import com.project.onTrackServer.model.Vendor;
import com.project.onTrackServer.service.VendorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vendors")
@CrossOrigin(origins = "*")
@Slf4j
public class VendorController {
    
    @Autowired
    private VendorService vendorService;
    
    @PostMapping
    public ResponseEntity<Vendor> createVendor(
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Vendor vendor) {
        log.info("Create vendor request");
        Vendor created = vendorService.createVendor(userId, vendor);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping("/{vendorId}")
    public ResponseEntity<Vendor> getVendor(@PathVariable Long vendorId) {
        log.info("Get vendor: {}", vendorId);
        return vendorService.getVendor(vendorId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/name/{vendorName}")
    public ResponseEntity<Vendor> getVendorByName(@PathVariable String vendorName) {
        log.info("Get vendor by name: {}", vendorName);
        return vendorService.getVendorByName(vendorName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping
    public ResponseEntity<List<Vendor>> getAllVendors() {
        log.info("Get all vendors");
        List<Vendor> vendors = vendorService.getAllVendors();
        return ResponseEntity.ok(vendors);
    }
    
    @PutMapping("/{vendorId}")
    public ResponseEntity<Vendor> updateVendor(
            @PathVariable Long vendorId,
            @RequestHeader("X-User-Id") String userId,
            @RequestBody Vendor vendor) {
        log.info("Update vendor: {}", vendorId);
        Vendor updated = vendorService.updateVendor(vendorId, userId, vendor);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{vendorId}")
    public ResponseEntity<ApiResponse> deleteVendor(
            @PathVariable Long vendorId,
            @RequestHeader("X-User-Id") String userId) {
        log.info("Delete vendor: {}", vendorId);
        vendorService.deleteVendor(vendorId, userId);
        return ResponseEntity.ok(new ApiResponse(true, "Vendor deleted successfully"));
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
