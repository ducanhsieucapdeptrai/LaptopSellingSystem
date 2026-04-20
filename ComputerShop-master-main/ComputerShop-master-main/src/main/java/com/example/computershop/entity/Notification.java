package com.example.computershop.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "Notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "NotificationID", columnDefinition = "UNIQUEIDENTIFIER")
    String notificationId;

    @Column(name = "UserID", columnDefinition = "UNIQUEIDENTIFIER")
    String userId;

    @Column(name = "Message", columnDefinition = "NVARCHAR(1000)", nullable = false)
    String message;

    @Column(name = "IsRead", columnDefinition = "BIT")
    Boolean isRead = false;

    @Column(name = "CreatedAt", columnDefinition = "DATETIME2")
    LocalDateTime createdAt;

    @Column(name = "OrderID", columnDefinition = "UNIQUEIDENTIFIER")
    String orderId;

    @Column(name = "ProductID", columnDefinition = "UNIQUEIDENTIFIER")
    String productId;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", referencedColumnName = "user_id", insertable = false, updatable = false)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OrderID", referencedColumnName = "OrderID", insertable = false, updatable = false)
    Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductID", referencedColumnName = "productID", insertable = false, updatable = false)
    Products product;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (isRead == null) {
            isRead = false;
        }
    }
} 