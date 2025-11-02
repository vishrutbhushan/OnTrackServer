package com.project.onTrackServer.service;

import com.project.onTrackServer.model.UserVendorMap;
import com.project.onTrackServer.model.User;
import com.project.onTrackServer.model.Vendor;
import com.project.onTrackServer.repository.UserVendorMapRepository;
import com.project.onTrackServer.repository.UserRepository;
import com.project.onTrackServer.repository.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserVendorMapService {
    
    @Autowired
    private UserVendorMapRepository userVendorMapRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private VendorRepository vendorRepository;
    
    public List<UserVendorMap> getAllActiveUserVendorMaps() {
        return userVendorMapRepository.findAllActive();
    }
    
    public List<UserVendorMap> getVendorsByUser(Long userId) {
        return userVendorMapRepository.findByUserIdAndNotDeleted(userId);
    }
    
    public List<UserVendorMap> getUsersByVendor(Long vendorId) {
        return userVendorMapRepository.findByVendorIdAndNotDeleted(vendorId);
    }
    
    public Optional<UserVendorMap> findByUserAndVendor(Long userId, Long vendorId) {
        Optional<User> user = userRepository.findById(userId);
        Optional<Vendor> vendor = vendorRepository.findById(vendorId);
        
        if (user.isPresent() && vendor.isPresent()) {
            return userVendorMapRepository.findByUserAndVendorAndNotDeleted(user.get(), vendor.get());
        }
        return Optional.empty();
    }
    
    public UserVendorMap createUserVendorMapping(Long userId, Long vendorId, Long createUserId) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Vendor> vendorOpt = vendorRepository.findById(vendorId);
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        
        if (vendorOpt.isEmpty()) {
            throw new RuntimeException("Vendor not found with ID: " + vendorId);
        }
        
        User user = userOpt.get();
        Vendor vendor = vendorOpt.get();
        
        // Check if mapping already exists
        Optional<UserVendorMap> existingMapping = userVendorMapRepository.findByUserAndVendorAndNotDeleted(user, vendor);
        if (existingMapping.isPresent()) {
            throw new RuntimeException("User-Vendor mapping already exists");
        }
        
        UserVendorMap userVendorMap = new UserVendorMap(user, vendor);
        userVendorMap.setCreateUser(createUserId);
        userVendorMap.setUpdateUser(createUserId);
        
        return userVendorMapRepository.save(userVendorMap);
    }
    
    public UserVendorMap updateUserVendorMapping(Long userVendorId, Long updateUserId) {
        Optional<UserVendorMap> userVendorMapOpt = userVendorMapRepository.findById(userVendorId);
        
        if (userVendorMapOpt.isEmpty()) {
            throw new RuntimeException("UserVendorMap not found with ID: " + userVendorId);
        }
        
        UserVendorMap userVendorMap = userVendorMapOpt.get();
        userVendorMap.setUpdateUser(updateUserId);
        userVendorMap.setUpdateTime(LocalDateTime.now());
        
        return userVendorMapRepository.save(userVendorMap);
    }
    
    public void deleteUserVendorMapping(Long userVendorId, Long deleteUserId) {
        Optional<UserVendorMap> userVendorMapOpt = userVendorMapRepository.findById(userVendorId);
        
        if (userVendorMapOpt.isEmpty()) {
            throw new RuntimeException("UserVendorMap not found with ID: " + userVendorId);
        }
        
        UserVendorMap userVendorMap = userVendorMapOpt.get();
        userVendorMap.setIsDeleted(true);
        userVendorMap.setUpdateUser(deleteUserId);
        userVendorMap.setUpdateTime(LocalDateTime.now());
        
        userVendorMapRepository.save(userVendorMap);
    }
    
    public void deleteByUserAndVendor(Long userId, Long vendorId, Long deleteUserId) {
        Optional<UserVendorMap> mapping = findByUserAndVendor(userId, vendorId);
        if (mapping.isPresent()) {
            deleteUserVendorMapping(mapping.get().getUserVendorId(), deleteUserId);
        } else {
            throw new RuntimeException("User-Vendor mapping not found");
        }
    }
}
