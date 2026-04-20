package com.example.computershop.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Order_Details")
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "OrderDetailID", columnDefinition = "UNIQUEIDENTIFIER")
     String orderDetailID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderID", referencedColumnName = "OrderID", columnDefinition = "UNIQUEIDENTIFIER")
     Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductID", referencedColumnName = "productID", columnDefinition = "UNIQUEIDENTIFIER")
     Products product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", referencedColumnName = "variant_id", columnDefinition = "UNIQUEIDENTIFIER")
     ProductVariant variant;

    @Column(name = "Quantity", nullable = false)
     Integer quantity;

    @Column(name = "UnitPrice", nullable = false)
     Long unitPrice;

    @Column(name = "TotalPrice", insertable = false, updatable = false)
     Long totalPrice;
    
    // Computed method to calculate total price when database computed column is null
    @Transient
    public Long getCalculatedTotalPrice() {
        if (totalPrice != null) {
            return totalPrice;
        }
        if (unitPrice != null && quantity != null) {
            return unitPrice * quantity;
        }
        return 0L;
    }
    
    @Override
    public int hashCode() {
        return orderDetailID != null ? orderDetailID.hashCode() : 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderDetail that = (OrderDetail) o;
        return orderDetailID != null && orderDetailID.equals(that.orderDetailID);
    }
} 
