package com.example.computershop.dto;

import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewDTO {
    String reviewId;
    String userId;
    String productId;
    Integer rating;
    String comment;
    LocalDateTime createdAt;
    
    // User info (without circular reference)
    UserInfo user;
    
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class UserInfo {
        String userId;
        String fullName;
        String image;
    }
} 