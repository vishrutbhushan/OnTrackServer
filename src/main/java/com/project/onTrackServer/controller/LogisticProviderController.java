package com.project.onTrackServer.controller;

import com.project.onTrackServer.model.ApiResponse;
import com.project.onTrackServer.model.LogisticProvider;
import com.project.onTrackServer.repository.LogisticProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/logistics")
@CrossOrigin(origins = "*")
public class LogisticProviderController {

    @Autowired
    private LogisticProviderRepository logisticProviderRepository;

    @PostMapping
    public ResponseEntity<ApiResponse<LogisticProvider>> createLogisticProvider(@RequestBody LogisticProvider provider) {
        try {
            if (logisticProviderRepository.existsByNameAndIsDeletedFalse(provider.getName())) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Logistic provider with this name already exists", null));
            }
            
            LogisticProvider savedProvider = logisticProviderRepository.save(provider);
            return ResponseEntity.ok(new ApiResponse<>(true, "Logistic provider created successfully", savedProvider));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error creating logistic provider: " + e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LogisticProvider>> getLogisticProviderById(@PathVariable Long id) {
        try {
            return logisticProviderRepository.findById(id)
                .map(provider -> ResponseEntity.ok(new ApiResponse<>(true, "Logistic provider found", provider)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Logistic provider not found", null)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error retrieving logistic provider: " + e.getMessage(), null));
        }
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ApiResponse<LogisticProvider>> getLogisticProviderByName(@PathVariable String name) {
        try {
            return logisticProviderRepository.findByNameAndIsDeletedFalse(name)
                .map(provider -> ResponseEntity.ok(new ApiResponse<>(true, "Logistic provider found", provider)))
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Logistic provider not found", null)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error retrieving logistic provider: " + e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LogisticProvider>>> getAllLogisticProviders() {
        try {
            List<LogisticProvider> providers = logisticProviderRepository.findAllByIsDeletedFalse();
            return ResponseEntity.ok(new ApiResponse<>(true, "Logistic providers retrieved successfully", providers));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error retrieving logistic providers: " + e.getMessage(), null));
        }
    }

    @GetMapping("/reliable")
    public ResponseEntity<ApiResponse<List<LogisticProvider>>> getReliableProviders() {
        try {
            List<LogisticProvider> providers = logisticProviderRepository.findByMaxDelayRisk(2);
            return ResponseEntity.ok(new ApiResponse<>(true, "Reliable logistic providers retrieved successfully", providers));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error retrieving reliable providers: " + e.getMessage(), null));
        }
    }

    @GetMapping("/rating/{minRating}")
    public ResponseEntity<ApiResponse<List<LogisticProvider>>> getProvidersByMinRating(@PathVariable BigDecimal minRating) {
        try {
            List<LogisticProvider> providers = logisticProviderRepository.findByMinRating(minRating);
            return ResponseEntity.ok(new ApiResponse<>(true, "Logistic providers with minimum rating retrieved successfully", providers));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error retrieving providers by rating: " + e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LogisticProvider>> updateLogisticProvider(@PathVariable Long id, @RequestBody LogisticProvider providerDetails) {
        try {
            Optional<LogisticProvider> optionalProvider = logisticProviderRepository.findById(id);
            if (optionalProvider.isPresent()) {
                LogisticProvider provider = optionalProvider.get();
                provider.setName(providerDetails.getName());
                provider.setAvgRating(providerDetails.getAvgRating());
                provider.setApiEndpoint(providerDetails.getApiEndpoint());
                provider.setCanDelay(providerDetails.getCanDelay());
                provider.setCanBeBadQuality(providerDetails.getCanBeBadQuality());
                provider.setUpdateTime(LocalDateTime.now());
                
                LogisticProvider updatedProvider = logisticProviderRepository.save(provider);
                return ResponseEntity.ok(new ApiResponse<>(true, "Logistic provider updated successfully", updatedProvider));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Logistic provider not found", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error updating logistic provider: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteLogisticProvider(@PathVariable Long id) {
        try {
            if (!logisticProviderRepository.findById(id).isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Logistic provider not found", null));
            }
            
            logisticProviderRepository.deleteById(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Logistic provider deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error deleting logistic provider: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}/soft")
    public ResponseEntity<ApiResponse<Void>> softDeleteLogisticProvider(@PathVariable Long id) {
        try {
            Optional<LogisticProvider> optionalProvider = logisticProviderRepository.findById(id);
            if (optionalProvider.isPresent()) {
                LogisticProvider provider = optionalProvider.get();
                provider.setIsDeleted(true);
                provider.setUpdateTime(LocalDateTime.now());
                logisticProviderRepository.save(provider);
                return ResponseEntity.ok(new ApiResponse<>(true, "Logistic provider soft deleted successfully", null));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Logistic provider not found", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error soft deleting logistic provider: " + e.getMessage(), null));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getLogisticProvidersCount() {
        try {
            long count = logisticProviderRepository.count();
            return ResponseEntity.ok(new ApiResponse<>(true, "Count retrieved successfully", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Error counting logistic providers: " + e.getMessage(), null));
        }
    }
}
