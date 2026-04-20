package com.example.computershop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.computershop.entity.ProductVariant;
import com.example.computershop.entity.Products;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, String> {
    
    List<ProductVariant> findByProductAndIsActiveTrue(Products product);
    
    List<ProductVariant> findByProduct_ProductID(String productId);
    
    List<ProductVariant> findByProduct_ProductIDAndIsActiveTrue(String productId);
    
    Optional<ProductVariant> findByProductAndCpuAndRamAndStorage(
        Products product, String cpu, String ram, String storage);
    
    @Query("SELECT v FROM ProductVariant v WHERE v.product.productID = :productId AND v.isActive = true ORDER BY v.price ASC")
    List<ProductVariant> findActiveVariantsByProductIdOrderByPrice(@Param("productId") String productId);
    
    boolean existsByProduct_ProductIDAndCpuAndRamAndStorage(
        String productId, String cpu, String ram, String storage);
} 