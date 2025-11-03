package com.project.onTrackServer.service;

import com.project.onTrackServer.model.Vendor;
import com.project.onTrackServer.repository.VendorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class VendorService {
    
    @Autowired
    private VendorRepository vendorRepository;
    
    public Vendor createVendor(String userId, Vendor vendorData) {
        log.info("Creating vendor: {}", vendorData.getVendorName());
        
        vendorData.setCreateUser(userId);
        vendorData.setUpdateUser(userId);
        
        return vendorRepository.save(vendorData);
    }
    
    public Optional<Vendor> getVendor(Long vendorId) {
        log.info("Fetching vendor: {}", vendorId);
        return vendorRepository.findById(vendorId);
    }
    
    public Optional<Vendor> getVendorByName(String vendorName) {
        log.info("Fetching vendor by name: {}", vendorName);
        return vendorRepository.findByVendorName(vendorName);
    }
    
    public List<Vendor> getAllVendors() {
        log.info("Fetching all active vendors");
        return vendorRepository.findByIsDeletedFalse();
    }
    
    public Vendor updateVendor(Long vendorId, String userId, Vendor vendorData) {
        log.info("Updating vendor: {}", vendorId);
        
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found"));
        
        vendor.setVendorName(vendorData.getVendorName());
        vendor.setVendorRating(vendorData.getVendorRating());
        vendor.setUpdateUser(userId);
        
        return vendorRepository.save(vendor);
    }
    
    public void deleteVendor(Long vendorId, String userId) {
        log.info("Deleting vendor: {}", vendorId);
        
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new IllegalArgumentException("Vendor not found"));
        
        vendor.setIsDeleted(true);
        vendor.setUpdateUser(userId);
        vendorRepository.save(vendor);
    }
}
