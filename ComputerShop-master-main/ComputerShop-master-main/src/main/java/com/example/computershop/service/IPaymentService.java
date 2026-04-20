package com.example.computershop.service;

import com.example.computershop.entity.Payment;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IPaymentService {

    Payment save(Payment payment);
    Optional<Payment> findById(UUID paymentId);
    List<Payment> findAll();
    void deleteById(UUID paymentId);
    boolean existsById(UUID paymentId);
    long count();
    List<Payment> findAll(Sort sort);

    Optional<Payment> findByOrderId(UUID orderId);
    List<Payment> findByUserId(UUID userId);
    Optional<Payment> findByVnpTxnRef(String vnpTxnRef);
    Optional<Payment> findByTransactionId(String transactionId);
    List<Payment> findByPaymentMethod(String paymentMethod);
    List<Payment> findByPaymentStatus(String paymentStatus);

    Payment createVNPayPayment(UUID orderId, UUID userId, Long amount, String vnpTxnRef);
    Payment updateVNPayPaymentResult(String vnpTxnRef, String vnpResponseCode, 
                                   String vnpTransactionStatus, String vnpPayDate, 
                                   String transactionId, String bankCode, String cardType);

    boolean hasSuccessfulPayment(UUID orderId);
    Long getTotalPaidAmountByOrderId(UUID orderId);

    List<Payment> findRecentPaymentsByUserId(UUID userId);
}
