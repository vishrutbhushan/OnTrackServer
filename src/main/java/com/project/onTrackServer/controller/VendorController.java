package com.project.onTrackServer.controller;

import com.project.onTrackServer.model.ApiResponse;
import com.project.onTrackServer.model.Vendor;
import com.project.onTrackServer.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/vendors")
@CrossOrigin(origins = "*")
public class VendorController {

    @Autowired
    private VendorRepository vendorRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<Vendor>> createVendor(@RequestBody Vendor vendor) {
        try {
            if (vendorRepository.existsByNameAndIsDeletedFalse(vendor.getName())) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Vendor with this name already exists", null));
            }
            
            Vendor savedVendor = vendorRepository.save(vendor);
            return ResponseEntity.ok(new ApiResponse<>(true, "Vendor created successfully", savedVendor));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error creating vendor: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Vendor>> getVendorById(@PathVariable Long id) {
        try {
            return vendorRepository.findByVendorIdAndIsDeletedFalse(id)
                .map(vendor -> ResponseEntity.ok(new ApiResponse<>(true, "Vendor found", vendor)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Vendor not found", null)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error retrieving vendor: " + e.getMessage(), null));
        }
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<Vendor>> getVendorByName(@PathVariable String name) {
        try {
            return vendorRepository.findByNameAndIsDeletedFalse(name)
                .map(vendor -> ResponseEntity.ok(new ApiResponse<>(true, "Vendor found", vendor)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Vendor not found", null)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error retrieving vendor: " + e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Vendor>>> getAllVendors() {
        try {
            List<Vendor> vendors = vendorRepository.findByIsDeletedFalse();
            return ResponseEntity.ok(new ApiResponse<>(true, "Vendors retrieved successfully", vendors));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error retrieving vendors: " + e.getMessage(), null));
        }
    }

    @GetMapping("/paginated")
    public ResponseEntity<ApiResponse<Page<Vendor>>> getAllVendorsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<Vendor> vendors = vendorRepository.findByIsDeletedFalse(pageable);
            return ResponseEntity.ok(new ApiResponse<>(true, "Vendors retrieved successfully", vendors));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error retrieving vendors: " + e.getMessage(), null));
        }
    }

    @GetMapping("/low-risk")
    public ResponseEntity<ApiResponse<List<Vendor>>> getLowRiskVendors() {
        try {
            List<Vendor> vendors = vendorRepository.findByCanDelayLessThanEqualAndCanBeBadQualityLessThanEqualAndIsDeletedFalse(2, 2);
            return ResponseEntity.ok(new ApiResponse<>(true, "Low risk vendors retrieved successfully", vendors));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error retrieving low risk vendors: " + e.getMessage(), null));
        }
    }

    @GetMapping("/high-quality")
    public ResponseEntity<ApiResponse<List<Vendor>>> getHighQualityVendors() {
        try {
            List<Vendor> vendors = vendorRepository.findByAvgRatingGreaterThanEqualAndIsDeletedFalse(4);
            return ResponseEntity.ok(new ApiResponse<>(true, "High quality vendors retrieved successfully", vendors));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error retrieving high quality vendors: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Vendor>> updateVendor(@PathVariable Long id, @RequestBody Vendor vendorDetails) {
        try {
            Optional<Vendor> optionalVendor = vendorRepository.findByVendorIdAndIsDeletedFalse(id);
            if (optionalVendor.isPresent()) {
                Vendor vendor = optionalVendor.get();
                vendor.setName(vendorDetails.getName());
                vendor.setAvgRating(vendorDetails.getAvgRating());
                vendor.setCanDelay(vendorDetails.getCanDelay());
                vendor.setCanBeBadQuality(vendorDetails.getCanBeBadQuality());
                vendor.setUpdateTime(LocalDateTime.now());
                
                Vendor updatedVendor = vendorRepository.save(vendor);
                return ResponseEntity.ok(new ApiResponse<>(true, "Vendor updated successfully", updatedVendor));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Vendor not found", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error updating vendor: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVendor(@PathVariable Long id) {
        try {
            if (!vendorRepository.findByVendorIdAndIsDeletedFalse(id).isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Vendor not found", null));
            }
            
            vendorRepository.deleteById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Vendor deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error deleting vendor: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}/soft")
    public ResponseEntity<ApiResponse<Void>> softDeleteVendor(@PathVariable Long id) {
        try {
            Optional<Vendor> optionalVendor = vendorRepository.findByVendorIdAndIsDeletedFalse(id);
            if (optionalVendor.isPresent()) {
                Vendor vendor = optionalVendor.get();
                vendor.setIsDeleted(true);
                vendor.setUpdateTime(LocalDateTime.now());
                vendorRepository.save(vendor);
                return ResponseEntity.ok(new ApiResponse<>(true, "Vendor soft deleted successfully", null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Vendor not found", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error soft deleting vendor: " + e.getMessage(), null));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getVendorsCount() {
        try {
            long count = vendorRepository.countByIsDeletedFalse();
            return ResponseEntity.ok(new ApiResponse<>(true, "Count retrieved successfully", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error counting vendors: " + e.getMessage(), null));
        }
    }
}
