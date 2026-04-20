package com.example.computershop.service.impl;

import com.example.computershop.service.DistanceService;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of DistanceService
 * Uses predefined distance table for Vietnamese cities/regions
 */
@Service
public class DistanceServiceImpl implements DistanceService {
    
    // Store location (FPT University Hanoi at Hoa Lac)
    private static final String STORE_ADDRESS = "Đại học FPT Hà Nội, Khu Công nghệ cao Hòa Lạc, Thạch Thất, Hà Nội";
    
    // Distance table from FPT Hanoi (Hoa Lac) to other regions (in kilometers)
    private static final Map<String, Double> DISTANCE_TABLE = new HashMap<>();
    
    static {
        DISTANCE_TABLE.put("ha-noi", 0.0);           // Same city (Hanoi)
        DISTANCE_TABLE.put("Hà Nội", 0.0);           // Same city (Vietnamese)
        DISTANCE_TABLE.put("ho-chi-minh", 1760.0);   // Hanoi to Ho Chi Minh
        DISTANCE_TABLE.put("Hồ Chí Minh", 1760.0);   // Hanoi to Ho Chi Minh (Vietnamese)
        DISTANCE_TABLE.put("da-nang", 800.0);        // Hanoi to Da Nang
        DISTANCE_TABLE.put("can-tho", 1900.0);       // Hanoi to Can Tho
        DISTANCE_TABLE.put("hai-phong", 120.0);      // Hanoi to Hai Phong
        DISTANCE_TABLE.put("other", 400.0);          // Default for other regions
    }
    
    @Override
    public double calculateDistance(String customerAddress, String customerCity, String customerRegion) {
        if (customerRegion == null || customerRegion.trim().isEmpty()) {
            return DISTANCE_TABLE.get("other");
        }
        
        String region = customerRegion.toLowerCase().trim();
        return DISTANCE_TABLE.getOrDefault(region, DISTANCE_TABLE.get("other"));
    }
    
    @Override
    public String getStoreAddress() {
        return STORE_ADDRESS;
    }
    
    /**
     * Calculate intra-city distance for same region (Hanoi area)
     * @param customerAddress Customer's specific address within Hanoi
     * @return Distance in kilometers (for Hanoi area delivery)
     */
    public double calculateIntraCityDistance(String customerAddress) {
        // Simple calculation based on address length as proxy for distance from Hoa Lac
        if (customerAddress == null || customerAddress.trim().isEmpty()) {
            return 15.0; // Default 15km from Hoa Lac to Hanoi center
        }
        
        // Basic algorithm: assume longer addresses are further from Hoa Lac
        int addressComplexity = customerAddress.length() / 20;
        return Math.min(10.0 + addressComplexity * 3, 40.0); // Min 10km, Max 40km for Hanoi area
    }
} 