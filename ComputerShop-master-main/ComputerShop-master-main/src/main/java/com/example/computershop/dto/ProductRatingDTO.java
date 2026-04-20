package com.example.computershop.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.util.Map;
import java.util.HashMap;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductRatingDTO {
    String productId;
    Double averageRating;
    Long totalReviews;
    Map<Integer, Long> ratingDistribution = new HashMap<>();

    public int getFullStars() {
        if (averageRating == null) return 0;
        return (int) Math.floor(averageRating);
    }
    
    public boolean hasHalfStar() {
        if (averageRating == null) return false;
        return (averageRating - Math.floor(averageRating)) >= 0.5;
    }
    
    public int getEmptyStars() {
        return 5 - getFullStars() - (hasHalfStar() ? 1 : 0);
    }

    public String getFormattedRating() {
        if (averageRating == null) return "0.0";
        return String.format("%.1f", averageRating);
    }

    public double getRatingPercentage(int rating) {
        if (totalReviews == null || totalReviews == 0) return 0.0;
        Long count = ratingDistribution.get(rating);
        if (count == null) return 0.0;
        return (count.doubleValue() / totalReviews.doubleValue()) * 100.0;
    }

} 