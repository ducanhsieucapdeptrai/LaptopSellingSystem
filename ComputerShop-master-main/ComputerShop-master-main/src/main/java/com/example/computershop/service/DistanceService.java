package com.example.computershop.service;

/**
 * Service for calculating distance between two locations
 */
public interface DistanceService {
    
    /**
     * Calculate distance between store and customer address
     * @param customerAddress Customer's full address
     * @param customerCity Customer's city
     * @param customerRegion Customer's region
     * @return Distance in kilometers
     */
    double calculateDistance(String customerAddress, String customerCity, String customerRegion);
    
    /**
     * Get store address for distance calculation
     * @return Store's address
     */
    String getStoreAddress();
} 