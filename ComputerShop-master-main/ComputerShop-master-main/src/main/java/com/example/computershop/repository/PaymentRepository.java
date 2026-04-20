package com.example.computershop.repository;

import com.example.computershop.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    
    // Find payment by order ID
    Optional<Payment> findByOrderId(UUID orderId);
    
    // Find payments by user ID
    List<Payment> findByUserId(UUID userId);
    
    // Find payment by VNPay transaction reference
    Optional<Payment> findByVnpTxnRef(String vnpTxnRef);
    
    // Find payment by transaction ID
    Optional<Payment> findByTransactionId(String transactionId);
    
    // Find payments by payment method
    List<Payment> findByPaymentMethod(String paymentMethod);
    
    // Find payments by status
    List<Payment> findByPaymentStatus(String paymentStatus);
    
    // Find payments by VNPay response code
    List<Payment> findByVnpResponseCode(String vnpResponseCode);
    
    // Get total paid amount for an order
    @Query("SELECT COALESCE(SUM(p.paidAmount), 0) FROM Payment p WHERE p.orderId = :orderId AND p.paymentStatus = 'Paid'")
    Long getTotalPaidAmountByOrderId(@Param("orderId") UUID orderId);
    
    // Check if order has successful payment
    @Query("SELECT COUNT(p) > 0 FROM Payment p WHERE p.orderId = :orderId AND p.paymentStatus = 'Paid'")
    boolean hasSuccessfulPayment(@Param("orderId") UUID orderId);
    
    // Find recent payments for a user
    @Query("SELECT p FROM Payment p WHERE p.userId = :userId ORDER BY p.createdAt DESC")
    List<Payment> findRecentPaymentsByUserId(@Param("userId") UUID userId);
}
