package com.example.computershop.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "Carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cart implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "CartID", columnDefinition = "UNIQUEIDENTIFIER")
    private String cartId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", columnDefinition = "nvarchar(255)")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "productid", columnDefinition = "nvarchar(255)")
    private Products product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", referencedColumnName = "variant_id", columnDefinition = "nvarchar(255)")
    private ProductVariant variant;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt = LocalDateTime.now();

    // Voucher fields
    @Column(name = "voucher_code", columnDefinition = "NVARCHAR(100)")
    private String voucherCode;

    @Column(name = "discount_amount")
    private Long discountAmount = 0L;

    public Cart(Products product, Integer quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    // Helper method to calculate final price after discount
    @Transient
    public Long getFinalPrice() {
        if (product == null) return 0L;
        
        Long originalPrice;
        if (variant != null) {
            originalPrice = variant.getPrice().longValue() * quantity;
        } else {
            originalPrice = product.getPrice().longValue() * quantity;
        }
        
        return originalPrice - (discountAmount != null ? discountAmount : 0L);
    }

    @Transient
    public Long getOriginalPrice() {
        if (product == null) return 0L;
        
        if (variant != null) {
            return variant.getPrice().longValue() * quantity;
        } else {
            return product.getPrice().longValue() * quantity;
        }
    }

    // getters, setters, constructors
}
