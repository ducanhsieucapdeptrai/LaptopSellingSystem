package com.example.computershop.service.impl;

import com.example.computershop.entity.Notification;
import com.example.computershop.entity.Order;
import com.example.computershop.entity.User;
import com.example.computershop.enums.Role;
import com.example.computershop.repository.NotificationRepository;
import com.example.computershop.repository.UserRepository;
import com.example.computershop.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Autowired
    public NotificationServiceImpl(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void createOrderSuccessNotification(User user, Order order) {
        String message = String.format(
                "Your order #%s has been placed successfully! Total amount: %,d VND. We will process and deliver it as soon as possible.",
                order.getId().substring(0, 8), order.getTotalAmount());

        Notification notification = Notification.builder()
                .userId(user.getUserId())
                .message(message)
                .orderId(order.getId())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }

    @Override
    public void createOrderStatusChangeNotification(Order order, String oldStatus, String newStatus) {
        Optional<User> userOpt = userRepository.findById(order.getUserId());
        if (userOpt.isEmpty()) {
            System.err.println("❌ User not found with ID: " + order.getUserId());
            return;
        }

        User user = userOpt.get();

        String message = String.format(
                "Order #%s status has been updated from '%s' to '%s'.",
                order.getId().substring(0, 8),
                getStatusDisplayName(oldStatus),
                getStatusDisplayName(newStatus));

        Notification notification = Notification.builder()
                .userId(user.getUserId())
                .message(message)
                .orderId(order.getId())
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);
    }

    @Override
    public void createNewOrderNotificationForAdmin(Order order) {
        List<User> adminUsers = userRepository.findByRole(Role.Admin);
        List<User> salesUsers = userRepository.findByRole(Role.Sales);

        String customerName = order.getCustomerName() != null ? order.getCustomerName() : "N/A";

        String message = String.format(
                "New order #%s from customer %s. Total amount: %,d VND. Please review and process.",
                order.getId().substring(0, 8), customerName, order.getTotalAmount());

        for (User salesUser : salesUsers) {
            Notification notification = Notification.builder()
                    .userId(salesUser.getUserId())
                    .message(message)
                    .orderId(order.getId())
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            notificationRepository.save(notification);
        }

        for (User admin : adminUsers) {
            Notification notification = Notification.builder()
                    .userId(admin.getUserId())
                    .message(message)
                    .orderId(order.getId())
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);
        }
    }

    @Override
    public void createBroadcastNotification(String message) {
        List<User> allUsers = userRepository.findByIsActive(true);

        for (User user : allUsers) {
            Notification notification = Notification.builder()
                    .userId(user.getUserId())
                    .message(message)
                    .isRead(false)
                    .createdAt(LocalDateTime.now())
                    .build();

            notificationRepository.save(notification);
        }
    }

    @Override
    public Notification createNotificationForUser(String userId, String message, String orderId, String productId) {
        Notification notification = Notification.builder()
                .userId(userId)
                .message(message)
                .orderId(orderId)
                .productId(productId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsByUserId(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotificationsByUserId(String userId) {
        return notificationRepository.findUnreadNotificationsByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadNotificationsByUserId(String userId) {
        return notificationRepository.countUnreadNotificationsByUserId(userId);
    }

    @Override
    public void markNotificationAsRead(String notificationId) {
        notificationRepository.markAsRead(notificationId);
    }

    @Override
    public void markAllNotificationsAsRead(String userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAllOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> searchNotifications(String search, String status, String type) {
        List<Notification> all = notificationRepository.findAllOrderByCreatedAtDesc();

        System.out.println("=== SEARCH NOTIFICATIONS DEBUG ===");
        System.out.println("Total notifications in database: " + all.size());
        System.out.println("Search term: " + search);
        System.out.println("Status filter: " + status);
        System.out.println("Type filter: " + type);

        return all.stream()
                .filter(n -> search == null || search.isEmpty() ||
                        (n.getMessage() != null && n.getMessage().toLowerCase().contains(search.toLowerCase())))
                .filter(n -> status == null || status.isEmpty() ||
                        ("unread".equals(status) && !Boolean.TRUE.equals(n.getIsRead())) ||
                        ("read".equals(status) && Boolean.TRUE.equals(n.getIsRead())))
                .filter(n -> type == null || type.isEmpty() ||
                        ("order".equals(type) && n.getOrderId() != null) ||
                        ("product".equals(type) && n.getProductId() != null) ||
                        ("system".equals(type) && n.getOrderId() == null && n.getProductId() == null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Notification getNotificationById(String notificationId) {
        Optional<Notification> notification = notificationRepository.findById(notificationId);
        return notification.orElse(null);
    }

    private String getStatusDisplayName(String status) {
        if (status == null) return "Unknown";

        switch (status) {
            case "PENDING":
                return "⏳ Pending Confirmation";
            case "PAYMENT_PENDING":
                return "💳 Payment Pending";
            case "CONFIRMED":
                return "✅ Confirmed";
            case "PROCESSING":
                return "🔄 Processing";
            case "SHIPPED":
                return "🚚 Shipping";
            case "DELIVERED":
                return "📦 Delivered";
            case "USER_CONFIRMED":
                return "✅ Received by Customer";
            case "CANCELLED":
                return "❌ Cancelled";
            default:
                return "❓ Unknown";
        }
    }
}