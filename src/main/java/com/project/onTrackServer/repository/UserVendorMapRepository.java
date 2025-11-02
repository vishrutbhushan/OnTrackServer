package com.project.onTrackServer.repository;

import com.project.onTrackServer.model.UserVendorMap;
import com.project.onTrackServer.model.User;
import com.project.onTrackServer.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserVendorMapRepository extends JpaRepository<UserVendorMap, Long> {
    
    @Query("SELECT uvm FROM UserVendorMap uvm WHERE uvm.isDeleted = false")
    List<UserVendorMap> findAllActive();
    
    @Query("SELECT uvm FROM UserVendorMap uvm WHERE uvm.user = :user AND uvm.isDeleted = false")
    List<UserVendorMap> findByUserAndNotDeleted(@Param("user") User user);
    
    @Query("SELECT uvm FROM UserVendorMap uvm WHERE uvm.vendor = :vendor AND uvm.isDeleted = false")
    List<UserVendorMap> findByVendorAndNotDeleted(@Param("vendor") Vendor vendor);
    
    @Query("SELECT uvm FROM UserVendorMap uvm WHERE uvm.user = :user AND uvm.vendor = :vendor AND uvm.isDeleted = false")
    Optional<UserVendorMap> findByUserAndVendorAndNotDeleted(@Param("user") User user, @Param("vendor") Vendor vendor);
    
    @Query("SELECT uvm FROM UserVendorMap uvm WHERE uvm.user.userId = :userId AND uvm.isDeleted = false")
    List<UserVendorMap> findByUserIdAndNotDeleted(@Param("userId") Long userId);
    
    @Query("SELECT uvm FROM UserVendorMap uvm WHERE uvm.vendor.vendorId = :vendorId AND uvm.isDeleted = false")
    List<UserVendorMap> findByVendorIdAndNotDeleted(@Param("vendorId") Long vendorId);
}
