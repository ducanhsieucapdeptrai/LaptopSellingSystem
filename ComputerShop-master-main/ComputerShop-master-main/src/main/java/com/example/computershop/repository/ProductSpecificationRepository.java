package com.example.computershop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.computershop.entity.ProductSpecifications;

public interface ProductSpecificationRepository extends JpaRepository<ProductSpecifications, String> {
    
    // Method to find specification by product ID
    @Query("SELECT ps FROM ProductSpecifications ps WHERE ps.productID = :productId")
    Optional<ProductSpecifications> findByProductId(@Param("productId") String productId);
    
    @Query("SELECT DISTINCT ps.cpu FROM ProductSpecifications ps WHERE ps.cpu IS NOT NULL ORDER BY ps.cpu")
    List<String> findDistinctCpus();
    
    @Query("SELECT DISTINCT ps.ram FROM ProductSpecifications ps WHERE ps.ram IS NOT NULL ORDER BY ps.ram")
    List<String> findDistinctRams();
    
    @Query("SELECT DISTINCT ps.ssd FROM ProductSpecifications ps WHERE ps.ssd IS NOT NULL ORDER BY ps.ssd")
    List<String> findDistinctSsds();
    
    @Query("SELECT DISTINCT ps.vga FROM ProductSpecifications ps WHERE ps.vga IS NOT NULL ORDER BY ps.vga")
    List<String> findDistinctVgas();
    
    @Query("SELECT DISTINCT ps.screen FROM ProductSpecifications ps WHERE ps.screen IS NOT NULL ORDER BY ps.screen")
    List<String> findDistinctScreens();
} 