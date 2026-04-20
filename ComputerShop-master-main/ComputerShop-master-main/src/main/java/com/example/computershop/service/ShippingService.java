package com.example.computershop.service;

import java.math.BigDecimal;

/**
 * Service for calculating shipping fees
 */
public interface ShippingService {
    
    /**
     * Calculate shipping fee based on distance and shipping method
     * @param distance Distance in kilometers
     * @param shippingMethod Shipping method (standard, express, etc.)
     * @return Shipping fee in VND
     */
    BigDecimal calculateShippingFee(double distance, String shippingMethod);
    
    /**
     * Get available shipping methods
     * @return Array of shipping method options
     */
    String[] getAvailableShippingMethods();
    
    /**
     * Get estimated delivery time based on distance and method
     * @param distance Distance in kilometers
     * @param shippingMethod Shipping method
     * @return Estimated delivery time in days
     */
    int getEstimatedDeliveryTime(double distance, String shippingMethod);
} 