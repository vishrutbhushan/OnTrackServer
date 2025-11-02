package com.project.onTrackServer.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_user", columnList = "fk_user"),
    @Index(name = "idx_orders_tracking", columnList = "tracking_no"),
    @Index(name = "idx_orders_status", columnList = "current_status")
})
public class Orders {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;
    
    @Column(name = "order_no", nullable = false)
    private String orderNo;
    
    @Column(name = "price_of_item")
    private Long priceOfItem;
    
    @Column(name = "product_link", length = 500)
    private String productLink;
    
    @Column(name = "tracking_no")
    private String trackingNo;
    
    @Column(name = "order_date")
    private LocalDateTime orderDate;
    
    @Column(name = "current_status", length = 100)
    private String currentStatus;
    
    @Column(name = "product_category")
    private String productCategory;
    
    @Column(name = "order_total", precision = 12, scale = 2)
    private BigDecimal orderTotal = BigDecimal.ZERO;
    
    @Column(name = "avg_rating")
    private Integer avgRating;
    
    @Column(name = "bad_quality_possible")
    private Integer badQualityPossible;
    
    @Column(name = "delay_possible")
    private Integer delayPossible;

    // Foreign Key Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_vendor")
    @JsonIgnore
    private Vendor vendor;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_logistic_provider")
    @JsonIgnore
    private LogisticProvider logisticProvider;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_user", nullable = false)
    @JsonIgnore
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_ecommerce_platform")
    @JsonIgnore
    private EcommercePlatform ecommercePlatform;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_product_categories")
    @JsonIgnore
    private ProductCategories productCategoriesEntity;
    
    // Audit fields
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

    // Constructors
    public Orders() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    // Lifecycle callbacks
    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public Long getPriceOfItem() {
        return priceOfItem;
    }

    public void setPriceOfItem(Long priceOfItem) {
        this.priceOfItem = priceOfItem;
    }

    public String getProductLink() {
        return productLink;
    }

    public void setProductLink(String productLink) {
        this.productLink = productLink;
    }

    public String getTrackingNo() {
        return trackingNo;
    }

    public void setTrackingNo(String trackingNo) {
        this.trackingNo = trackingNo;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public BigDecimal getOrderTotal() {
        return orderTotal;
    }

    public void setOrderTotal(BigDecimal orderTotal) {
        this.orderTotal = orderTotal;
    }

    public Integer getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(Integer avgRating) {
        this.avgRating = avgRating;
    }

    public Integer getBadQualityPossible() {
        return badQualityPossible;
    }

    public void setBadQualityPossible(Integer badQualityPossible) {
        this.badQualityPossible = badQualityPossible;
    }

    public Integer getDelayPossible() {
        return delayPossible;
    }

    public void setDelayPossible(Integer delayPossible) {
        this.delayPossible = delayPossible;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public LogisticProvider getLogisticProvider() {
        return logisticProvider;
    }

    public void setLogisticProvider(LogisticProvider logisticProvider) {
        this.logisticProvider = logisticProvider;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public EcommercePlatform getEcommercePlatform() {
        return ecommercePlatform;
    }

    public void setEcommercePlatform(EcommercePlatform ecommercePlatform) {
        this.ecommercePlatform = ecommercePlatform;
    }

    public ProductCategories getProductCategoriesEntity() {
        return productCategoriesEntity;
    }

    public void setProductCategoriesEntity(ProductCategories productCategoriesEntity) {
        this.productCategoriesEntity = productCategoriesEntity;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Long getCreateUser() {
        return createUser;
    }

    public void setCreateUser(Long createUser) {
        this.createUser = createUser;
    }

    public Long getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(Long updateUser) {
        this.updateUser = updateUser;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
