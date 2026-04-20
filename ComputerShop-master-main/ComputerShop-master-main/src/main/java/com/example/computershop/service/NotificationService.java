package com.example.computershop.service;

import com.example.computershop.entity.Notification;
import com.example.computershop.entity.Order;
import com.example.computershop.entity.User;

import java.util.List;

public interface NotificationService {
    
    // Tạo notification cho user khi đặt hàng thành công
    void createOrderSuccessNotification(User user, Order order);
    
    // Tạo notification khi trạng thái đơn hàng thay đổi
    void createOrderStatusChangeNotification(Order order, String oldStatus, String newStatus);
    
    // Tạo notification cho admin khi có đơn hàng mới
    void createNewOrderNotificationForAdmin(Order order);
    
    // Tạo notification cho tất cả users (admin broadcast)
    void createBroadcastNotification(String message);
    
    // Tạo notification cho user cụ thể
    Notification createNotificationForUser(String userId, String message, String orderId, String productId);
    
    // Lấy tất cả notifications của user
    List<Notification> getNotificationsByUserId(String userId);
    
    // Lấy notifications chưa đọc của user
    List<Notification> getUnreadNotificationsByUserId(String userId);
    
    // Đếm số notifications chưa đọc của user
    long countUnreadNotificationsByUserId(String userId);
    
    // Đánh dấu notification đã đọc
    void markNotificationAsRead(String notificationId);
    
    // Đánh dấu tất cả notifications của user đã đọc
    void markAllNotificationsAsRead(String userId);
    
    // Lấy tất cả notifications (dành cho admin)
    List<Notification> getAllNotifications();
    
    // Tìm notification theo ID
    Notification getNotificationById(String notificationId);

    // Tìm kiếm notifications với filters
    List<Notification> searchNotifications(String search, String status, String type);
    

} 