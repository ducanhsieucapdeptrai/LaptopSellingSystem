package com.example.computershop.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.computershop.entity.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, String> {
    
    // Lấy reviews theo productId với phân trang
    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.user WHERE r.productId = :productId ORDER BY r.createdAt DESC")
    Page<Review> findByProductIdOrderByCreatedAtDesc(@Param("productId") String productId, Pageable pageable);
    
    // Tính rating trung bình của sản phẩm
    @Query("SELECT AVG(CAST(r.rating AS double)) FROM Review r WHERE r.productId = :productId")
    Double getAverageRatingByProductId(@Param("productId") String productId);
    
    // Đếm tổng số reviews của sản phẩm
    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId")
    Long getTotalReviewsByProductId(@Param("productId") String productId);
    
    // Lấy phân bố rating (số lượng mỗi loại rating)
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.productId = :productId GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> getRatingDistributionByProductId(@Param("productId") String productId);
    
    // Kiểm tra user đã review sản phẩm chưa
    boolean existsByUserIdAndProductId(String userId, String productId);
    
    // Lấy review của user cho sản phẩm cụ thể
    Optional<Review> findByUserIdAndProductId(String userId, String productId);

    // Top 5 sản phẩm có rating cao nhất
    @Query("SELECT r.productId, AVG(CAST(r.rating AS double)), COUNT(r) FROM Review r GROUP BY r.productId HAVING COUNT(r) >= 3 ORDER BY AVG(CAST(r.rating AS double)) DESC")
    List<Object[]> getTop5HighestRatedProducts(Pageable pageable);
} 