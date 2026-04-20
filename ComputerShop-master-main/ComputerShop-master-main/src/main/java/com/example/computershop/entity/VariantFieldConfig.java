package com.example.computershop.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.ObjectMapper;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Entity
@Table(name = "VariantFieldConfigs")
public class VariantFieldConfig {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "field_id", unique = true, nullable = false, columnDefinition = "nvarchar(255)")
    String fieldId;
    
    @Column(name = "field_name", columnDefinition = "nvarchar(255)", nullable = false)
    String fieldName;
    
    @Column(name = "field_key", columnDefinition = "nvarchar(100)", unique = true, nullable = false)
    String fieldKey;
    
    @Column(name = "field_type", columnDefinition = "nvarchar(50)")
    String fieldType = "TEXT";
    
    @Column(name = "field_options", columnDefinition = "nvarchar(max)")
    String fieldOptions;
    
    @Column(name = "is_required", columnDefinition = "bit")
    Boolean isRequired = false;
    
    @Column(name = "is_active", columnDefinition = "bit")
    Boolean isActive = true;
    
    @Column(name = "display_order", columnDefinition = "int")
    Integer displayOrder = 0;
    
    @Column(name = "created_at", columnDefinition = "datetime")
    LocalDateTime createdAt = LocalDateTime.now();

    public String[] getOptionsArray() {
        if (fieldOptions == null || fieldOptions.isEmpty()) {
            return new String[0];
        }
        try {

            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(fieldOptions, String[].class);
        } catch (Exception e) {
            return fieldOptions.replaceAll("[\\[\\]\"]", "").split(",");
        }
    }
    
    @PreUpdate
    public void preUpdate() {
        // Auto update timestamp if needed
    }
} 