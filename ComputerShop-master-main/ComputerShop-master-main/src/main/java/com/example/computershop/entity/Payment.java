package com.example.computershop.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "Payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id", columnDefinition = "UNIQUEIDENTIFIER")
    private UUID paymentId;
    
    @Column(name = "order_id", columnDefinition = "UNIQUEIDENTIFIER")
    private UUID orderId;
    
    @Column(name = "user_id", columnDefinition = "UNIQUEIDENTIFIER")  
    private UUID userId;
    
    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // e.g., VNPAY, COD
    
    @Column(name = "payment_status", length = 50)
    private String paymentStatus; // Pending, Paid, Failed
    
    @Column(name = "paid_amount")
    private Long paidAmount;
    
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    
    @Column(name = "transaction_id")
    private String transactionId;
    
    @Column(name = "vnp_txn_ref")
    private String vnpTxnRef;
    
    // VNPay Response Fields
    @Column(name = "vnp_response_code")
    private String vnpResponseCode;  // Mã phản hồi từ VNPay
    
    @Column(name = "vnp_transaction_status")
    private String vnpTransactionStatus; // Trạng thái giao dịch từ VNPay
    
    @Column(name = "vnp_secure_hash")
    private String vnpSecureHash; // Hash để verify
    
    @Column(name = "vnp_pay_date")
    private String vnpPayDate; // Thời gian thanh toán từ VNPay (format: yyyyMMddHHmmss)
    
    @Column(name = "bank_code")
    private String bankCode; // Mã ngân hàng
    
    @Column(name = "card_type")
    private String cardType; // Loại thẻ
    
    @Column(name = "order_info")
    private String orderInfo; // Thông tin đơn hàng
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Constructor for convenience
    public Payment(UUID orderId, String paymentMethod, String paymentStatus, Long paidAmount) {
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.paidAmount = paidAmount;
    }
    
    // Constructor with userId
    public Payment(UUID orderId, UUID userId, String paymentMethod, String paymentStatus, Long paidAmount) {
        this.orderId = orderId;
        this.userId = userId;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.paidAmount = paidAmount;
    }
} 