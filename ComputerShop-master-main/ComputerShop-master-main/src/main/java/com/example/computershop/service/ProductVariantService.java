package com.example.computershop.service;

import com.example.computershop.entity.ProductVariant;
import com.example.computershop.entity.Products;
import com.example.computershop.repository.ProductVariantRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ProductVariantService {
    
    private final ProductVariantRepository variantRepository;
    private final ProductService productService;
    
    public List<ProductVariant> getVariantsByProduct(String productId) {
        return variantRepository.findByProduct_ProductID(productId);
    }
    
    public List<ProductVariant> getActiveVariantsByProduct(String productId) {
        return variantRepository.findByProduct_ProductIDAndIsActiveTrue(productId);
    }
    
    public List<ProductVariant> getActiveVariantsByProductSorted(String productId) {
        return variantRepository.findActiveVariantsByProductIdOrderByPrice(productId);
    }
    
    public ProductVariant findById(String variantId) {
        return variantRepository.findById(variantId).orElse(null);
    }
    
    public ProductVariant findVariantBySpecs(String productId, String cpu, String ram, String storage) {
        Products product = productService.findById(productId);
        if (product == null) {
            return null;
        }
        return variantRepository.findByProductAndCpuAndRamAndStorage(product, cpu, ram, storage)
                .orElse(null);
    }
    
    @Transactional
    public ProductVariant save(ProductVariant variant) {
        return variantRepository.save(variant);
    }
    
    @Transactional
    public ProductVariant create(ProductVariant variant) {
        // Generate SKU if not provided
        if (variant.getSku() == null || variant.getSku().isEmpty()) {
            variant.setSku(generateSku(variant));
        }
        return variantRepository.save(variant);
    }
    
    @Transactional
    public ProductVariant update(String variantId, ProductVariant updatedVariant) {
        Optional<ProductVariant> existingVariant = variantRepository.findById(variantId);
        if (existingVariant.isPresent()) {
            ProductVariant variant = existingVariant.get();
            variant.setVariantName(updatedVariant.getVariantName());
            variant.setPrice(updatedVariant.getPrice());
            variant.setQuantity(updatedVariant.getQuantity());
            variant.setCpu(updatedVariant.getCpu());
            variant.setRam(updatedVariant.getRam());
            variant.setStorage(updatedVariant.getStorage());
            variant.setGpu(updatedVariant.getGpu());
            variant.setScreen(updatedVariant.getScreen());
            variant.setIsActive(updatedVariant.getIsActive());
            
            // CRITICAL FIX: Update image and custom attributes that were missing
            if (updatedVariant.getVariantImageUrl() != null) {
                variant.setVariantImageUrl(updatedVariant.getVariantImageUrl());
            }
            if (updatedVariant.getCustomAttributes() != null) {
                variant.setCustomAttributes(updatedVariant.getCustomAttributes());
            }
            if (updatedVariant.getVariantImages() != null) {
                variant.setVariantImages(updatedVariant.getVariantImages());
            }
            
            return variantRepository.save(variant);
        }
        return null;
    }
    
    @Transactional
    public boolean delete(String variantId) {
        try {
            variantRepository.deleteById(variantId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean existsBySpecs(String productId, String cpu, String ram, String storage) {
        return variantRepository.existsByProduct_ProductIDAndCpuAndRamAndStorage(productId, cpu, ram, storage);
    }
    
    private String generateSku(ProductVariant variant) {
        String productCode = variant.getProduct().getProductID().substring(0, 8);
        String cpuCode = variant.getCpu() != null ? variant.getCpu().substring(0, Math.min(3, variant.getCpu().length())) : "XXX";
        String ramCode = variant.getRam() != null ? variant.getRam().replaceAll("\\D", "") : "00";
        String storageCode = variant.getStorage() != null ? variant.getStorage().replaceAll("\\D", "") : "00";
        return productCode + "-" + cpuCode + "-" + ramCode + "-" + storageCode;
    }
} 