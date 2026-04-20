package com.example.computershop.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.computershop.entity.VariantFieldConfig;

@Repository
public interface VariantFieldConfigRepository extends JpaRepository<VariantFieldConfig, String> {
    List<VariantFieldConfig> findByIsActiveTrueOrderByDisplayOrder();
    boolean existsByFieldKey(String fieldKey);
    List<VariantFieldConfig> findAllByOrderByDisplayOrder();
} 