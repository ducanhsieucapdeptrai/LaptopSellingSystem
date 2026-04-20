package com.example.computershop.dto.request;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO for checkout parameters to reduce method parameter count
 * Updated to remove duplicate fields with User entity
 */
@Getter
@Setter
public class CheckoutRequest {

    
    // ✅ KEPT - Shipping information (có thể khác với user.address)
    private String shippingAddress;
    private String city;
    private String region;
    private String district;
    private String ward;
    private String note;
    private String paymentMethod;
    private String shippingMethod;
    private Double distance;
    private Long shippingFee;
    
    // ✅ NEW - Alternative receiver information (nếu khác với user)
    private String alternativeReceiverName;
    private String alternativeReceiverPhone;

    /**
     * Default constructor required for JSON deserialization and framework compatibility
     */
    public CheckoutRequest() {
        // Intentionally empty - default values for all fields are sufficient
        // Spring Boot and Jackson require this for JSON deserialization
    }

    // Helper method để build complete shipping address
    public String getCompleteShippingAddress() {
        StringBuilder address = new StringBuilder();
        
        appendAddressComponent(address, shippingAddress);
        appendAddressComponent(address, ward);
        appendAddressComponent(address, district);
        appendAddressComponent(address, city);
        appendAddressComponent(address, region);
        
        return address.toString();
    }
    
    /**
     * Helper method to append address component with proper comma separation
     */
    private void appendAddressComponent(StringBuilder address, String component) {
        if (component != null && !component.trim().isEmpty()) {
            if (!address.isEmpty()) {
                address.append(", ");
            }
            address.append(component);
        }
    }
} 