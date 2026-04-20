package com.example.computershop.service;

import com.example.computershop.entity.ProductSpecifications;
import com.example.computershop.repository.ProductSpecificationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class ProductSpecificationService {

    private final ProductSpecificationRepository specificationRepository;
    
    public ProductSpecifications findByProductId(String productId) {
        log.debug("Finding specification for product ID: {}", productId);
        return specificationRepository.findByProductId(productId).orElse(null);
    }
    
    @Transactional
    public ProductSpecifications save(ProductSpecifications specification) {
        log.debug("Saving specification for product ID: {}", specification.getProductID());
        
        if (specification.getProductID() == null) {
            throw new IllegalArgumentException("Product ID cannot be null when saving specification");
        }
        
        ProductSpecifications saved = specificationRepository.save(specification);
        log.info("Successfully saved specification for product ID: {}", saved.getProductID());
        return saved;
    }
} 