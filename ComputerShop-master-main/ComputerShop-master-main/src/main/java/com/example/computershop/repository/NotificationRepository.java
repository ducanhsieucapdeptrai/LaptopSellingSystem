package com.example.computershop.repository;

import com.example.computershop.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    
    // Tìm notifications theo userId, sắp xếp theo thời gian tạo mới nhất
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdOrderByCreatedAtDesc(@Param("userId") String userId);
    
    // Tìm notifications chưa đọc của user
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadNotificationsByUserId(@Param("userId") String userId);
    
    // Đếm số notification chưa đọc của user
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false")
    long countUnreadNotificationsByUserId(@Param("userId") String userId);
    
    // Tìm notifications theo orderId
    List<Notification> findByOrderId(String orderId);
    
    // Tìm notifications theo productId
    List<Notification> findByProductId(String productId);
    
    // Đánh dấu notification đã đọc
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.notificationId = :notificationId")
    void markAsRead(@Param("notificationId") String notificationId);
    
    // Đánh dấu tất cả notifications của user đã đọc
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId")
    void markAllAsReadByUserId(@Param("userId") String userId);
    
    // Tìm tất cả notifications trong khoảng thời gian
    @Query("SELECT n FROM Notification n WHERE n.createdAt BETWEEN :startDate AND :endDate ORDER BY n.createdAt DESC")
    List<Notification> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Tìm tất cả notifications (dành cho admin)
    @Query("SELECT n FROM Notification n ORDER BY n.createdAt DESC")
    List<Notification> findAllOrderByCreatedAtDesc();
    
    // Xóa notifications cũ hơn số ngày nhất định
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :beforeDate")
    void deleteNotificationsOlderThan(@Param("beforeDate") LocalDateTime beforeDate);
    
    @Query("SELECT n FROM Notification n WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           " LOWER(n.message) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(n.user.fullName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           " LOWER(n.user.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND (:status IS NULL OR :status = '' OR " +
           " (:status = 'read' AND n.isRead = true) OR " +
           " (:status = 'unread' AND n.isRead = false)) " +
           "AND (:type IS NULL OR :type = '' OR " +
           " (:type = 'order' AND n.orderId IS NOT NULL) OR " +
           " (:type = 'product' AND n.productId IS NOT NULL) OR " +
           " (:type = 'system' AND n.orderId IS NULL AND n.productId IS NULL)) " +
           "ORDER BY n.createdAt DESC")
    List<Notification> findNotificationsWithFilters(
        @Param("search") String search,
        @Param("status") String status,
        @Param("type") String type
    );
}