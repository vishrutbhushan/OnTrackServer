package com.project.onTrackServer.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_categories")
public class ProductCategories {
    
    @Id
    @Column(name = "product_categories_no", nullable = false)
    private Long productCategoriesNo;
    
    @Column(name = "product_categories", length = 250)
    private String productCategories;
    
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;
    
    @Column(name = "create_user")
    private Long createUser;
    
    @Column(name = "update_user")
    private Long updateUser;
    
    @Column(name = "create_time")
    private LocalDateTime createTime;
    
    @Column(name = "update_time")
    private LocalDateTime updateTime;
    
    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
    
    // Constructors
    public ProductCategories() {}
    
    public ProductCategories(String productCategories) {
        this();
        this.productCategories = productCategories;
    }
    
    // Getters and Setters
    public Long getProductCategoriesNo() { return productCategoriesNo; }
    public void setProductCategoriesNo(Long productCategoriesNo) { this.productCategoriesNo = productCategoriesNo; }
    
    public String getProductCategories() { return productCategories; }
    public void setProductCategories(String productCategories) { this.productCategories = productCategories; }
    
    public Boolean getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
    
    public Long getCreateUser() { return createUser; }
    public void setCreateUser(Long createUser) { this.createUser = createUser; }
    
    public Long getUpdateUser() { return updateUser; }
    public void setUpdateUser(Long updateUser) { this.updateUser = updateUser; }
    
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
    
    @Override
    public String toString() {
        return "ProductCategories{" +
                "productCategoriesNo=" + productCategoriesNo +
                ", productCategories='" + productCategories + '\'' +
                '}';
    }
}
