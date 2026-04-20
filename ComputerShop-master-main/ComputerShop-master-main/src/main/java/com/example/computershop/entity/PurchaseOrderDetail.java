package com.example.computershop.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigInteger;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Entity
@Table(name = "purchase_order_detail")
public class PurchaseOrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false, columnDefinition = "nvarchar(255)")
    String id;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "productID")
    Products product;

    @Column(name = "quantity", columnDefinition = "int")
    Integer quantity;

    @Column(name = "import_price", columnDefinition = "bigint")
    BigInteger importPrice;

    @ManyToOne
    @JoinColumn(name = "purchase_order_id", referencedColumnName = "id")
    PurchaseOrder purchaseOrder;
} 