package com.example.computershop.service;

import com.example.computershop.entity.VariantFieldConfig;
import com.example.computershop.repository.VariantFieldConfigRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@AllArgsConstructor
public class VariantFieldConfigService {

    private final VariantFieldConfigRepository repository;

    public List<VariantFieldConfig> getAllActiveFields() {
        return repository.findByIsActiveTrueOrderByDisplayOrder();
    }

    public List<VariantFieldConfig> getAllFields() {
        return repository.findAllByOrderByDisplayOrder();
    }

    public VariantFieldConfig findById(String fieldId) {
        return repository.findById(fieldId).orElse(null);
    }

    @Transactional
    public VariantFieldConfig create(VariantFieldConfig fieldConfig) {
        if (repository.existsByFieldKey(fieldConfig.getFieldKey())) {
            throw new RuntimeException("Field key đã tồn tại!");
        }
        return repository.save(fieldConfig);
    }

    @Transactional
    public VariantFieldConfig update(String fieldId, VariantFieldConfig updatedField) {
        VariantFieldConfig existing = findById(fieldId);
        if (existing == null) {
            return null;
        }

        existing.setFieldName(updatedField.getFieldName());
        existing.setFieldType(updatedField.getFieldType());
        existing.setFieldOptions(updatedField.getFieldOptions());
        existing.setIsRequired(updatedField.getIsRequired());
        existing.setDisplayOrder(updatedField.getDisplayOrder());

        // Only update isActive if it's provided (not null)
        if (updatedField.getIsActive() != null) {
            existing.setIsActive(updatedField.getIsActive());
        }

        // Don't update fieldKey as it should be immutable

        return repository.save(existing);
    }

    @Transactional
    public boolean toggleStatus(String fieldId) {
        try {
            VariantFieldConfig field = repository.findById(fieldId).orElse(null);
            if (field != null) {
                field.setIsActive(!field.getIsActive());
                repository.save(field);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public boolean delete(String fieldId) {
        try {
            repository.deleteById(fieldId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}