package com.project.onTrackServer.service;

import com.project.onTrackServer.model.LogisticProvider;
import com.project.onTrackServer.repository.LogisticProviderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class LogisticProviderService {
    
    @Autowired
    private LogisticProviderRepository logisticProviderRepository;
    
    public LogisticProvider createProvider(String userId, LogisticProvider providerData) {
        log.info("Creating logistic provider: {}", providerData.getProviderName());
        
        providerData.setCreateUser(userId);
        providerData.setUpdateUser(userId);
        
        return logisticProviderRepository.save(providerData);
    }
    
    public Optional<LogisticProvider> getProvider(Long providerId) {
        log.info("Fetching logistic provider: {}", providerId);
        return logisticProviderRepository.findById(providerId);
    }
    
    public Optional<LogisticProvider> getProviderByName(String providerName) {
        log.info("Fetching logistic provider by name: {}", providerName);
        return logisticProviderRepository.findByProviderName(providerName);
    }
    
    public List<LogisticProvider> getAllProviders() {
        log.info("Fetching all active logistic providers");
        return logisticProviderRepository.findByIsDeletedFalse();
    }
    
    public LogisticProvider updateProvider(Long providerId, String userId, LogisticProvider providerData) {
        log.info("Updating logistic provider: {}", providerId);
        
        LogisticProvider provider = logisticProviderRepository.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Logistic provider not found"));
        
        provider.setProviderName(providerData.getProviderName());
        provider.setProviderRating(providerData.getProviderRating());
        provider.setUpdateUser(userId);
        
        return logisticProviderRepository.save(provider);
    }
    
    public void deleteProvider(Long providerId, String userId) {
        log.info("Deleting logistic provider: {}", providerId);
        
        LogisticProvider provider = logisticProviderRepository.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("Logistic provider not found"));
        
        provider.setIsDeleted(true);
        provider.setUpdateUser(userId);
        logisticProviderRepository.save(provider);
    }
}
