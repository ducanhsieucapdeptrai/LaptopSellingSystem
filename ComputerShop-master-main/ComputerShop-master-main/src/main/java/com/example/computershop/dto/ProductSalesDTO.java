package com.example.computershop.dto;

import java.math.BigDecimal;

import com.example.computershop.entity.Products;

public class ProductSalesDTO {
    private Products product;
    private Long totalSold;
    private BigDecimal totalRevenue;

    public ProductSalesDTO() {}

    public ProductSalesDTO(Products product, Long totalSold) {
        this.product = product;
        this.totalSold = totalSold;
        this.totalRevenue = BigDecimal.ZERO;
    }

    public ProductSalesDTO(Products product, Long totalSold, BigDecimal totalRevenue) {
        this.product = product;
        this.totalSold = totalSold;
        this.totalRevenue = totalRevenue;
    }

    // Getters and Setters
    public Products getProduct() { return product; }
    public void setProduct(Products product) { this.product = product; }

    public Long getTotalSold() { return totalSold; }
    public void setTotalSold(Long totalSold) { this.totalSold = totalSold; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
}
