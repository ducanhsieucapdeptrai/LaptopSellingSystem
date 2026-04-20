package com.example.computershop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "Vouchers")
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "voucherID", columnDefinition = "UNIQUEIDENTIFIER")
    private String voucherId;

    @Column(name = "code", columnDefinition = "NVARCHAR(100)", unique = true, nullable = false)
    private String code;

    @Column(name = "discount_percent")
    private Integer discountPercent;

    @Column(name = "max_discount_amount")
    private Integer maxDiscountAmount;

    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    @Column(name = "is_active", columnDefinition = "BIT DEFAULT 1")
    private Boolean isActive = true;

    // Usage tracking fields
    @Column(name = "last_used_by", columnDefinition = "UNIQUEIDENTIFIER")
    private String lastUsedBy;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "usage_count")
    private Integer usageCount = 0;
    
    // Manual getters to override Lombok
    public String getVoucherId() {
        return voucherId;
    }
    
    public String getCode() {
        return code;
    }
    
    public Integer getDiscountPercent() {
        return discountPercent;
    }
    
    public Integer getMaxDiscountAmount() {
        return maxDiscountAmount;
    }
    
    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public String getLastUsedBy() {
        return lastUsedBy;
    }
    
    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }
    
    // Safe getter for usageCount to handle null values
    public Integer getUsageCount() {
        return usageCount != null ? usageCount : 0;
    }
    
    // Safe setter for usageCount to prevent null values
    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount != null ? usageCount : 0;
    }
    
    // Method to ensure voucher is properly initialized
    public void ensureProperInitialization() {
        if (this.usageCount == null) {
            this.usageCount = 0;
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
    }
    
    // Automatically initialize after loading from database
    @PostLoad
    @PostPersist
    @PostUpdate
    public void postLoad() {
        ensureProperInitialization();
    }
} 