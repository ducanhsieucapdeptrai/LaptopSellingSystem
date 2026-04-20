package com.example.computershop.repository;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.computershop.dto.ProductSalesDTO;
import com.example.computershop.entity.Products;

public interface ProductRepository extends JpaRepository<Products, String> {
    boolean existsByName(String name);
    
    @Query("SELECT DISTINCT p.brand FROM Products p WHERE p.brand IS NOT NULL ORDER BY p.brand")
    List<String> findDistinctBrands();
    
    // Search products by name (case-insensitive)
    @Query("SELECT p FROM Products p LEFT JOIN FETCH p.specifications LEFT JOIN FETCH p.categories " +
           "WHERE p.isActive = true AND LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Products> findActiveProductsByNameContaining(@Param("searchTerm") String searchTerm);
    
    // Get product suggestions for autocomplete (limited results)
    @Query("SELECT p FROM Products p WHERE p.isActive = true AND LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Products> findProductSuggestions(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    // Optimized method to fetch products with specifications in one query
    @Query("SELECT p FROM Products p LEFT JOIN FETCH p.specifications LEFT JOIN FETCH p.categories WHERE p.isActive = true")
    List<Products> findAllActiveWithSpecifications();
    
    // Method to fetch all products with specifications (for admin)
    @Query("SELECT p FROM Products p LEFT JOIN FETCH p.specifications LEFT JOIN FETCH p.categories")
    List<Products> findAllWithSpecifications();
    
    // Method for homepage - fetch only needed data without specifications
    @Query("SELECT p FROM Products p LEFT JOIN FETCH p.categories WHERE p.isActive = true")
    List<Products> findAllActiveForHomepage();

    @Query("SELECT p FROM Products p ORDER BY p.quantity DESC")
    List<Products> findTop5ProductsByStock(Pageable pageable);

    @Query("SELECT new com.example.computershop.dto.ProductSalesDTO(od.product, SUM(od.quantity)) " +
            "FROM OrderDetail od " +
            "GROUP BY od.product " +
            "ORDER BY SUM(od.quantity) DESC")
    List<ProductSalesDTO> findTop5BestSellingProducts(Pageable pageable);

    @Query("SELECT COUNT(p) FROM Products p")
    long countProducts();


}