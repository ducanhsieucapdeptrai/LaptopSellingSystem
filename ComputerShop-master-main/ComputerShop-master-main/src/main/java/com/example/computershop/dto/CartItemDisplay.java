package com.example.computershop.dto;

import com.example.computershop.entity.Products;
import com.example.computershop.entity.ProductVariant;

/**
 * Display DTO for cart items in templates
 * Includes support for variants, vouchers, and price calculations
 */
public class CartItemDisplay {
    private Products product;
    private ProductVariant variant;
    private Integer quantity;
    private String voucherCode;
    private Long discountAmount;
    private Long originalPrice;
    private Long finalPrice;
    
    // Constructor for simple cart item without variant
    public CartItemDisplay(Products product, Integer quantity) {
        this.product = product;
        this.quantity = quantity;
        this.discountAmount = 0L;
        calculatePrices();
    }
    
    // Constructor for cart item with variant
    public CartItemDisplay(Products product, ProductVariant variant, Integer quantity) {
        this.product = product;
        this.variant = variant;
        this.quantity = quantity;
        this.discountAmount = 0L;
        calculatePrices();
    }
    
    // Constructor for cart item with voucher discount
    public CartItemDisplay(Products product, ProductVariant variant, Integer quantity, String voucherCode, Long discountAmount) {
        this.product = product;
        this.variant = variant;
        this.quantity = quantity;
        this.voucherCode = voucherCode;
        this.discountAmount = discountAmount != null ? discountAmount : 0L;
        calculatePrices();
    }
    
    /**
     * Calculate original and final prices based on product/variant and discount
     */
    private void calculatePrices() {
        if (product == null || quantity == null) {
            this.originalPrice = 0L;
            this.finalPrice = 0L;
            return;
        }
        
        Long unitPrice;
        if (variant != null) {
            unitPrice = variant.getPrice().longValue();
        } else {
            unitPrice = product.getPrice().longValue();
        }
        
        this.originalPrice = unitPrice * quantity;
        this.finalPrice = this.originalPrice - (this.discountAmount != null ? this.discountAmount : 0L);
    }

    // Getters
    public Products getProduct() { 
        return product; 
    }
    
    public ProductVariant getVariant() { 
        return variant; 
    }
    
    public Integer getQuantity() { 
        return quantity; 
    }
    
    public String getVoucherCode() { 
        return voucherCode; 
    }
    
    public Long getDiscountAmount() { 
        return discountAmount != null ? discountAmount : 0L; 
    }
    
    public Long getOriginalPrice() { 
        return originalPrice; 
    }
    
    public Long getFinalPrice() { 
        return finalPrice; 
    }
    
    public boolean hasDiscount() { 
        return discountAmount != null && discountAmount > 0; 
    }
    
    public boolean hasVariant() { 
        return variant != null; 
    }
} 