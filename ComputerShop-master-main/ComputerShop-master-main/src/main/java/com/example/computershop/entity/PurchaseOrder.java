package com.example.computershop.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Entity
@Table(name = "purchase_order")
public class PurchaseOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false, columnDefinition = "nvarchar(255)")
    String id;

    @Column(name = "created_at", columnDefinition = "datetime")
    LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "total_amount", columnDefinition = "bigint")
    BigInteger totalAmount;

    @Column(name = "paid_amount", columnDefinition = "bigint")
    BigInteger paidAmount;

    @Column(name = "debt_amount", columnDefinition = "bigint")
    BigInteger debtAmount;

    @Column(name = "status", columnDefinition = "nvarchar(50)")
    String status; // PAID, PARTIAL, UNPAID

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<PurchaseOrderDetail> details;
} 