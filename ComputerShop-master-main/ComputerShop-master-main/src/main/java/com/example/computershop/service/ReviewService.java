package com.example.computershop.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.computershop.dto.ProductRatingDTO;
import com.example.computershop.dto.ReviewDTO;
import com.example.computershop.entity.Order;
import com.example.computershop.entity.OrderDetail;
import com.example.computershop.entity.Review;
import com.example.computershop.repository.OrderRepository;
import com.example.computershop.repository.ReviewRepository;
import com.example.computershop.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);
    
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    
    /**
     * Convert Review entity to ReviewDTO
     */
    private ReviewDTO convertToDTO(Review review) {
        ReviewDTO.UserInfo userInfo = null;
        
        if (review.getUser() != null) {
            userInfo = ReviewDTO.UserInfo.builder()
                    .userId(review.getUser().getUserId())
                    .fullName(review.getUser().getFullName())
                    .image(review.getUser().getImage())
                    .build();
        }
        
        return ReviewDTO.builder()
                .reviewId(review.getReviewId())
                .userId(review.getUserId())
                .productId(review.getProductId())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .user(userInfo)
                .build();
    }

    /**
     * Lấy thống kê rating của sản phẩm
     */
    public ProductRatingDTO getProductRating(String productId) {
        try {
            ProductRatingDTO ratingDTO = ProductRatingDTO.builder()
                    .productId(productId)
                    .averageRating(reviewRepository.getAverageRatingByProductId(productId))
                    .totalReviews(reviewRepository.getTotalReviewsByProductId(productId))
                    .build();
            
            // Lấy phân bố rating
            List<Object[]> distribution = reviewRepository.getRatingDistributionByProductId(productId);
            Map<Integer, Long> ratingMap = new HashMap<>();
            
            for (Object[] row : distribution) {
                Integer rating = (Integer) row[0];
                Long count = (Long) row[1];
                ratingMap.put(rating, count);
            }
            ratingDTO.setRatingDistribution(ratingMap);
            
            return ratingDTO;
        } catch (Exception e) {
            logger.error("Error getting product rating for productId: {}", productId, e);
            return ProductRatingDTO.builder()
                    .productId(productId)
                    .averageRating(0.0)
                    .totalReviews(0L)
                    .ratingDistribution(new HashMap<>())
                    .build();
        }
    }
    
    /**
     * Lấy danh sách reviews của sản phẩm (return DTO)
     */
    public Page<ReviewDTO> getProductReviewsDTO(String productId, Pageable pageable) {
        Page<Review> reviews = reviewRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable);
        return reviews.map(this::convertToDTO);
    }
    
    /**
     * Lấy danh sách reviews của sản phẩm (return Entity - for internal use)
     */
    public Page<Review> getProductReviews(String productId, Pageable pageable) {
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable);
    }

    /**
     * Kiểm tra user có thể review sản phẩm không
     * User chỉ có thể review nếu:
     * 1. Đã mua sản phẩm này trong đơn hàng có status USER_CONFIRMED
     */
    public boolean canUserReview(String userId, String productId) {
        if (userId == null || productId == null) return false;
        
        try {
            // Kiểm tra user có đơn hàng USER_CONFIRMED chứa sản phẩm này không
            List<Order> deliveredOrders = orderRepository.findByUserIdAndStatus(userId, "USER_CONFIRMED");
            
            for (Order order : deliveredOrders) {
                // Lấy order với details
                Order orderWithDetails = orderRepository.findByIdWithDetails(order.getId()).orElse(null);
                if (orderWithDetails != null && orderWithDetails.getOrderDetails() != null) {
                    for (OrderDetail detail : orderWithDetails.getOrderDetails()) {
                        if (detail.getProduct() != null && productId.equals(detail.getProduct().getProductID())) {
                            logger.debug("User {} can review product {} from user-confirmed order {}",
                                       userId, productId, order.getId());
                            return true;
                        }
                    }
                }
            }
            
            logger.debug("User {} cannot review product {} - no user-confirmed order found", userId, productId);
            return false;
            
        } catch (Exception e) {
            logger.error("Error checking if user {} can review product {}: {}", userId, productId, e.getMessage());
            return false;
        }
    }
    
    /**
     * Tạo review mới hoặc cập nhật review hiện có
     */
    @Transactional
    public Review UpdateReview(String userId, String productId, Integer rating, String comment) {
        try {
            // Validation
            if (!canUserReview(userId, productId)) {
                throw new IllegalArgumentException("User cannot review this product");
            }
            
            if (rating < 1 || rating > 5) {
                throw new IllegalArgumentException("Rating must be between 1 and 5");
            }
            
            // Kiểm tra xem đã có review hay chưa
            Optional<Review> existingReview = reviewRepository.findByUserIdAndProductId(userId, productId);
            
            if (existingReview.isPresent()) {
                // Cập nhật review hiện có
                Review review = existingReview.get();
                review.setRating(rating);
                review.setComment(comment);
                return reviewRepository.save(review);
            } else {
                // Tạo review mới
                Review review = Review.builder()
                        .userId(userId)
                        .productId(productId)
                        .rating(rating)
                        .comment(comment)
                        .build();
                
                return reviewRepository.save(review);
            }
        } catch (Exception e) {
            logger.error("Error creating/updating review for user: {} product: {}", userId, productId, e);
            throw new RuntimeException("Failed to create or update review", e);
        }
    }
    
    /**
     * Tạo review mới
     */
    @Transactional
    public Review createReview(String userId, String productId, Integer rating, String comment) {
        try {
            // Validation
            if (!canUserReview(userId, productId)) {
                throw new IllegalArgumentException("User cannot review this product");
            }
            
            // Kiểm tra user đã review sản phẩm này chưa
            if (reviewRepository.existsByUserIdAndProductId(userId, productId)) {
                throw new IllegalArgumentException("User already reviewed this product");
            }
            
            if (rating < 1 || rating > 5) {
                throw new IllegalArgumentException("Rating must be between 1 and 5");
            }
            
            Review review = Review.builder()
                    .userId(userId)
                    .productId(productId)
                    .rating(rating)
                    .comment(comment)
                    .build();
            
            return reviewRepository.save(review);
        } catch (Exception e) {
            logger.error("Error creating review for user: {} product: {}", userId, productId, e);
            throw new RuntimeException("Failed to create review", e);
        }
    }
    
    /**
     * Cập nhật review
     */
    @Transactional
    public Review updateReview(String reviewId, String userId, Integer rating, String comment) {
        try {
            Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
            if (reviewOpt.isEmpty()) {
                throw new IllegalArgumentException("Review not found");
            }
            
            Review review = reviewOpt.get();
            
            // Kiểm tra quyền sửa (chỉ người tạo mới được sửa)
            if (!review.getUserId().equals(userId)) {
                throw new IllegalArgumentException("User cannot edit this review");
            }
            
            if (rating < 1 || rating > 5) {
                throw new IllegalArgumentException("Rating must be between 1 and 5");
            }
            
            review.setRating(rating);
            review.setComment(comment);
            
            return reviewRepository.save(review);
        } catch (Exception e) {
            logger.error("Error updating review: {}", reviewId, e);
            throw new RuntimeException("Failed to update review", e);
        }
    }
    
    /**
     * Xóa review
     */
    @Transactional
    public boolean deleteReview(String reviewId, String userId) {
        try {
            Optional<Review> reviewOpt = reviewRepository.findById(reviewId);
            if (reviewOpt.isEmpty()) {
                return false;
            }
            
            Review review = reviewOpt.get();
            
            // Kiểm tra quyền xóa (chỉ người tạo hoặc admin)
            if (!review.getUserId().equals(userId)) {
                throw new IllegalArgumentException("User cannot delete this review");
            }
            
            reviewRepository.delete(review);
            return true;
        } catch (Exception e) {
            logger.error("Error deleting review: {}", reviewId, e);
            return false;
        }
    }
    
    /**
     * Lấy review của user cho sản phẩm
     */
    public Optional<Review> getUserReviewForProduct(String userId, String productId) {
        return reviewRepository.findByUserIdAndProductId(userId, productId);
    }
} 