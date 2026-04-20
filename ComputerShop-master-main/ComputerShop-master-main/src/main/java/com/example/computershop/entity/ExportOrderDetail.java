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
@Table(name = "export_order_detail")
public class ExportOrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false, columnDefinition = "nvarchar(255)")
    String id;

    @ManyToOne
    @JoinColumn(name = "product_id", referencedColumnName = "productID")
    Products product;

    @Column(name = "quantity", columnDefinition = "int")
    Integer quantity;

    @Column(name = "export_price", columnDefinition = "bigint")
    BigInteger exportPrice;

    @ManyToOne
    @JoinColumn(name = "export_order_id", referencedColumnName = "id")
    ExportOrder exportOrder;
} 