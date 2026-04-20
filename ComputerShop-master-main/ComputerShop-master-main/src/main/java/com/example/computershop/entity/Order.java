package com.example.computershop.entity;

import jakarta.persistence.Entity;
import lombok.experimental.FieldDefaults;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "Orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "OrderID", columnDefinition = "UNIQUEIDENTIFIER")
     String id;

    @Column(name = "UserID", columnDefinition = "UNIQUEIDENTIFIER")
     String userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "OrderDate", nullable = false)
     LocalDateTime orderDate;

    @Column(name = "Status", columnDefinition = "NVARCHAR(50)")
     String status;

    @Column(name = "TotalAmount", nullable = false)
     Long totalAmount;

    @Column(name = "voucherID", columnDefinition = "UNIQUEIDENTIFIER")
     String voucherId;
    
    @Column(name = "original_amount")
    private Long originalAmount;

    @Column(name = "discount_amount")
    private Long discountAmount = 0L;

    @Column(name = "voucher_code", columnDefinition = "NVARCHAR(100)")
    private String voucherCode;


    @Column(name = "PaymentMethod", columnDefinition = "NVARCHAR(50)")
    private String paymentMethod;
    
    @Column(name = "Note", columnDefinition = "NVARCHAR(500)")
    private String note;

    // ✅ KEPT - Shipping information (có thể khác với user.address)
    @Column(name = "ShippingAddress", columnDefinition = "NVARCHAR(500)")
    private String shippingAddress;

    @Column(name = "ShippingMethod", columnDefinition = "NVARCHAR(50)")
    private String shippingMethod;
    
    @Column(name = "ShippingFee")
    private Long shippingFee;
    
    @Column(name = "Distance")
    private Double distance;

    @Column(name = "AlternativeReceiverName", columnDefinition = "NVARCHAR(255)")
    private String alternativeReceiverName;
    
    @Column(name = "AlternativeReceiverPhone", columnDefinition = "NVARCHAR(50)")
    private String alternativeReceiverPhone;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails;

    @Transient
    public String getCustomerName() {
        if (alternativeReceiverName != null && !alternativeReceiverName.trim().isEmpty()) {
            return alternativeReceiverName;
        }
        return user != null ? user.getFullName() : null;
    }
    
    @Transient
    public String getCustomerEmail() {
        return user != null ? user.getEmail() : null;
    }
    
    @Transient
    public String getCustomerPhone() {
        if (alternativeReceiverPhone != null && !alternativeReceiverPhone.trim().isEmpty()) {
            return alternativeReceiverPhone;
        }
        return user != null ? user.getPhoneNumber() : null;
    }
    
    @Transient
    public String getCustomerAddress() {
        return user != null ? user.getAddress() : null;
    }
    
    @Transient
    public String getPaymentStatus() {
        // TODO: Implement payment status lookup from Payment entity
        // This would require injecting PaymentRepository or adding a relationship
        // For now, return a default based on paymentMethod
        if ("VNPAY".equals(paymentMethod)) {
            return "PENDING"; // Will be updated by VNPay IPN
        } else if ("COD".equals(paymentMethod)) {
            return "COD"; // COD doesn't have online payment status
        }
        return "UNKNOWN";
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id != null && id.equals(order.id);
    }
} 
