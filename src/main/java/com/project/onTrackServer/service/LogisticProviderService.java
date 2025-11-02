package com.project.onTrackServer.service;

import com.project.onTrackServer.model.LogisticProvider;
import com.project.onTrackServer.repository.LogisticProviderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class LogisticProviderService {

    private final LogisticProviderRepository logisticProviderRepository;

    @Autowired
    public LogisticProviderService(LogisticProviderRepository logisticProviderRepository) {
        this.logisticProviderRepository = logisticProviderRepository;
    }

    public LogisticProvider save(LogisticProvider provider) {
        if (provider.getLogisticId() == null) {
            provider.setCreateTime(LocalDateTime.now());
        }
        provider.setUpdateTime(LocalDateTime.now());
        return logisticProviderRepository.save(provider);
    }

    public Optional<LogisticProvider> findById(Long id) {
        return logisticProviderRepository.findById(id);
    }

    public Optional<LogisticProvider> findByName(String name) {
        return logisticProviderRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public List<LogisticProvider> findAllActive() {
        return logisticProviderRepository.findAllActive();
    }

    @Transactional(readOnly = true)
    public Page<LogisticProvider> findAllActive(Pageable pageable) {
        return logisticProviderRepository.findAllActive(pageable);
    }

    @Transactional(readOnly = true)
    public List<LogisticProvider> findByMinimumRating(double minRating) {
        return logisticProviderRepository.findByMinimumRating(minRating);
    }

    @Transactional(readOnly = true)
    public List<LogisticProvider> findReliableProviders() {
        return logisticProviderRepository.findReliableProviders();
    }

    @Transactional(readOnly = true)
    public List<LogisticProvider> findLowRiskProviders() {
        return logisticProviderRepository.findLowRiskProviders();
    }

    @Transactional(readOnly = true)
    public List<LogisticProvider> findHighQualityProviders() {
        return logisticProviderRepository.findHighQualityProviders();
    }

    public void softDelete(Long id) {
        logisticProviderRepository.softDeleteById(id);
    }

    public void restore(Long id) {
        logisticProviderRepository.restoreById(id);
    }

    public LogisticProvider update(Long id, LogisticProvider updatedProvider) {
        return findById(id)
                .map(provider -> {
                    provider.setName(updatedProvider.getName());
                    provider.setAvgRating(updatedProvider.getAvgRating());
                    provider.setApiEndpoint(updatedProvider.getApiEndpoint());
                    provider.setCanDelay(updatedProvider.getCanDelay());
                    provider.setCanBeBadQuality(updatedProvider.getCanBeBadQuality());
                    provider.setUpdateTime(LocalDateTime.now());
                    return save(provider);
                })
                .orElseThrow(() -> new RuntimeException("Logistic provider not found with id: " + id));
    }

    public void deleteById(Long id) {
        logisticProviderRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return logisticProviderRepository.findByName(name).isPresent();
    }

    @Transactional(readOnly = true)
    public long countActiveProviders() {
        return logisticProviderRepository.countActiveProviders();
    }
}
