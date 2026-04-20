package com.example.computershop.repository;

import com.example.computershop.entity.Cart;
import com.example.computershop.entity.User;
import com.example.computershop.entity.Products;
import com.example.computershop.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, String> {
    List<Cart> findByUser(User user);
    List<Cart> findByUserUserId(String userId);
    
    //  NEW - Methods for finding cart items with specific product/variant combinations
    Optional<Cart> findByUserAndProductAndVariant(User user, Products product, ProductVariant variant);
    Optional<Cart> findByUserAndProductAndVariantIsNull(User user, Products product);
    
    //  NEW - Methods for cart management
    void deleteByUser(User user);
} 