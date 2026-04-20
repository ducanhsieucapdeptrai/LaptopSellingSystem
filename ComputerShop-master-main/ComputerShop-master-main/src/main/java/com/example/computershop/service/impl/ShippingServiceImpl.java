package com.example.computershop.service.impl;

import com.example.computershop.service.ShippingService;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Implementation of ShippingService
 * Calculates shipping fees based on distance and shipping method
 */
@Service
public class ShippingServiceImpl implements ShippingService {
    
    // Base shipping rates per km (in VND)
    private static final BigDecimal STANDARD_RATE_PER_KM = new BigDecimal("2000");   // 2,000 VND per km
    private static final BigDecimal EXPRESS_RATE_PER_KM = new BigDecimal("3500");    // 3,500 VND per km
    private static final BigDecimal OVERNIGHT_RATE_PER_KM = new BigDecimal("5000");  // 5,000 VND per km
    
    // Base fees (minimum charges)
    private static final BigDecimal STANDARD_BASE_FEE = new BigDecimal("30000");     // 30,000 VND
    private static final BigDecimal EXPRESS_BASE_FEE = new BigDecimal("50000");      // 50,000 VND
    private static final BigDecimal OVERNIGHT_BASE_FEE = new BigDecimal("80000");    // 80,000 VND
    
    // Free shipping threshold
    private static final double FREE_SHIPPING_DISTANCE = 20.0; // Free shipping within 20km from Hoa Lac
    
    @Override
    public BigDecimal calculateShippingFee(double distance, String shippingMethod) {
        if (shippingMethod == null) {
            shippingMethod = "standard";
        }
        
        shippingMethod = shippingMethod.toLowerCase().trim();
        
        // Free shipping for very short distances (same city center)
        if (distance <= FREE_SHIPPING_DISTANCE && "standard".equals(shippingMethod)) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal baseFee;
        BigDecimal ratePerKm;
        
        switch (shippingMethod) {
            case "express":
                baseFee = EXPRESS_BASE_FEE;
                ratePerKm = EXPRESS_RATE_PER_KM;
                break;
            case "overnight":
                baseFee = OVERNIGHT_BASE_FEE;
                ratePerKm = OVERNIGHT_RATE_PER_KM;
                break;
            case "standard":
            default:
                baseFee = STANDARD_BASE_FEE;
                ratePerKm = STANDARD_RATE_PER_KM;
                break;
        }
        
        // Calculate distance-based fee
        BigDecimal distanceFee = ratePerKm.multiply(BigDecimal.valueOf(distance));
        
        // Total fee = base fee + distance fee
        BigDecimal totalFee = baseFee.add(distanceFee);
        
        // Round to nearest 1000 VND
        return totalFee.divide(new BigDecimal("1000"), 0, RoundingMode.HALF_UP)
                      .multiply(new BigDecimal("1000"));
    }
    
    @Override
    public String[] getAvailableShippingMethods() {
        return new String[]{"standard", "express", "overnight"};
    }
    
    @Override
    public int getEstimatedDeliveryTime(double distance, String shippingMethod) {
        if (shippingMethod == null) {
            shippingMethod = "standard";
        }
        
        shippingMethod = shippingMethod.toLowerCase().trim();
        
        switch (shippingMethod) {
            case "overnight":
                return 1; // Next day delivery
            case "express":
                if (distance <= 50) return 1;      // Same day for short distances
                if (distance <= 200) return 2;     // 2 days for medium distances
                return 3;                          // 3 days for long distances
            case "standard":
            default:
                if (distance <= 50) return 2;      // 2 days for short distances
                if (distance <= 500) return 4;     // 4 days for medium distances
                if (distance <= 1000) return 6;    // 6 days for long distances
                return 8;                          // 8 days for very long distances
        }
    }
    
    /**
     * Get shipping method display name in Vietnamese
     * @param method Shipping method code
     * @return Display name in Vietnamese
     */
    public String getShippingMethodDisplayName(String method) {
        if (method == null) return "Standard";
        
        switch (method.toLowerCase().trim()) {
            case "express":
                return "Fast Delivery";
            case "overnight":
                return "Delivery in day";
            case "standard":
            default:
                return "Standard Delivery";
        }
    }
} 