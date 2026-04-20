package com.example.computershop.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;
import com.fasterxml.jackson.databind.ObjectMapper;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Entity
@Table(name = "product_variants")
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "variant_id", unique = true, nullable = false, columnDefinition = "nvarchar(255)")
    String variantId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "productid", columnDefinition = "nvarchar(255)")
    Products product;
    
    @Column(name = "variant_name", columnDefinition = "nvarchar(255)")
    String variantName; // VD: "Core i5 - 16GB RAM - 512GB SSD"
    
    @Column(name = "price", columnDefinition = "bigint", nullable = false)
    BigInteger price;
    
    @Column(name = "quantity", columnDefinition = "int")
    Integer quantity = 0;
    
    @Column(name = "sku", columnDefinition = "nvarchar(100)")
    String sku; // Mã SKU riêng cho từng variant
    
    @Column(name = "is_active", columnDefinition = "bit")
    Boolean isActive = true;
    
    // Các thuộc tính cấu hình
    @Column(name = "cpu", columnDefinition = "nvarchar(255)")
    String cpu;
    
    @Column(name = "ram", columnDefinition = "nvarchar(255)")
    String ram;
    
    @Column(name = "storage", columnDefinition = "nvarchar(255)")
    String storage;
    
    @Column(name = "gpu", columnDefinition = "nvarchar(255)")
    String gpu;
    
    @Column(name = "screen", columnDefinition = "nvarchar(255)")
    String screen;

    @Column(name = "variant_image_url", columnDefinition = "nvarchar(255)")
    String variantImageUrl;
    
    @Column(name = "variant_images", columnDefinition = "nvarchar(max)")
    String variantImages;
    
    @Column(name = "custom_attributes", columnDefinition = "nvarchar(max)")
    String customAttributes;
    
    @Column(name = "created_at", columnDefinition = "datetime")
    LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at", columnDefinition = "datetime")
    LocalDateTime updatedAt;
    
    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Map<String, String> getCustomAttributesMap() {
        if (customAttributes == null || customAttributes.isEmpty()) {
            return new HashMap<>();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(customAttributes, Map.class);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    public void setCustomAttributesMap(Map<String, String> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            this.customAttributes = null;
            return;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.customAttributes = mapper.writeValueAsString(attributes);
        } catch (Exception e) {
            this.customAttributes = null;
        }
    }

    public String[] getVariantImagesArray() {
        if (variantImages == null || variantImages.isEmpty()) {
            return new String[0];
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(variantImages, String[].class);
        } catch (Exception e) {
            return new String[0];
        }
    }

    public void setVariantImagesArray(String[] images) {
        if (images == null || images.length == 0) {
            this.variantImages = null;
            return;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.variantImages = mapper.writeValueAsString(images);
        } catch (Exception e) {
            this.variantImages = null;
        }
    }

    public String getDisplayImageUrl() {
        return (variantImageUrl != null && !variantImageUrl.isEmpty())
               ? variantImageUrl 
               : (product != null ? product.getImageURL() : null);
    }

    public String getDisplayName() {
        StringBuilder name = new StringBuilder();
        if (cpu != null && !cpu.isEmpty()) {
            name.append(cpu);
        }
        if (ram != null && !ram.isEmpty()) {
            if (name.length() > 0) name.append(" - ");
            name.append(ram);
        }
        if (storage != null && !storage.isEmpty()) {
            if (name.length() > 0) name.append(" - ");
            name.append(storage);
        }
        if (gpu != null && !gpu.isEmpty()) {
            if (name.length() > 0) name.append(" - ");
            name.append(gpu);
        }

        Map<String, String> customAttrs = getCustomAttributesMap();
        for (Map.Entry<String, String> entry : customAttrs.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                if (name.length() > 0) name.append(" - ");
                name.append(entry.getValue());
            }
        }
        
        return name.length() > 0 ? name.toString() : variantName;
    }
} 