package com.example.computershop.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.computershop.dto.ProductRatingDTO;
import com.example.computershop.dto.ReviewDTO;
import com.example.computershop.entity.Review;
import com.example.computershop.entity.User;
import com.example.computershop.service.ReviewService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    
    private final ReviewService reviewService;
    
    /**
     * Lấy thống kê rating của sản phẩm
     */
    @GetMapping("/product/{productId}/rating")
    public ResponseEntity<ProductRatingDTO> getProductRating(@PathVariable String productId) {
        ProductRatingDTO rating = reviewService.getProductRating(productId);
        return ResponseEntity.ok(rating);
    }
    
    /**
     * Lấy danh sách reviews của sản phẩm
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<ReviewDTO>> getProductReviews(
            @PathVariable String productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer rating) {
        
        Pageable pageable = PageRequest.of(page, size);
        
        // Hiện tại chỉ support getProductReviewsDTO, rating filter có thể implement sau
        Page<ReviewDTO> reviews = reviewService.getProductReviewsDTO(productId, pageable);
        
        return ResponseEntity.ok(reviews);
    }
    
    /**
     * Kiểm tra user có thể review không
     */
    @GetMapping("/can-review/{productId}")
    public ResponseEntity<Map<String, Object>> canUserReview(
            @PathVariable String productId,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            response.put("canReview", false);
            response.put("reason", "User not authenticated");
            return ResponseEntity.ok(response);
        }
        
        User user = (User) authentication.getPrincipal();
        boolean canReview = reviewService.canUserReview(user.getUserId(), productId);
        
        response.put("canReview", canReview);
        if (!canReview) {
            response.put("reason", "User already reviewed this product");
        }
        
        // Kiểm tra có review hiện tại không
        Optional<Review> existingReview = reviewService.getUserReviewForProduct(user.getUserId(), productId);
        if (existingReview.isPresent()) {
            response.put("existingReview", existingReview.get());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Tạo review mới
     */
    @PostMapping("/product/{productId}")
    public ResponseEntity<Map<String, Object>> createReview(
            @PathVariable String productId,
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            response.put("success", false);
            response.put("message", "User not authenticated");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            User user = (User) authentication.getPrincipal();
            Integer rating = (Integer) request.get("rating");
            String comment = (String) request.get("comment");
            
            Review review = reviewService.createReview(user.getUserId(), productId, rating, comment);
            
            response.put("success", true);
            response.put("message", "Review created successfully");
            response.put("reviewId", review.getReviewId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Cập nhật review
     */
    @PutMapping("/{reviewId}")
    public ResponseEntity<Map<String, Object>> updateReview(
            @PathVariable String reviewId,
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            response.put("success", false);
            response.put("message", "User not authenticated");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            User user = (User) authentication.getPrincipal();
            Integer rating = (Integer) request.get("rating");
            String comment = (String) request.get("comment");
            
            Review review = reviewService.updateReview(reviewId, user.getUserId(), rating, comment);
            
            response.put("success", true);
            response.put("message", "Review updated successfully");
            response.put("reviewId", review.getReviewId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Xóa review
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Map<String, Object>> deleteReview(
            @PathVariable String reviewId,
            Authentication authentication) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            response.put("success", false);
            response.put("message", "User not authenticated");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            User user = (User) authentication.getPrincipal();
            boolean deleted = reviewService.deleteReview(reviewId, user.getUserId());
            
            if (deleted) {
                response.put("success", true);
                response.put("message", "Review deleted successfully");
            } else {
                response.put("success", false);
                response.put("message", "Failed to delete review");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
} 