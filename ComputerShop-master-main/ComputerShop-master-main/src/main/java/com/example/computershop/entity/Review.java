package com.example.computershop.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
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
@Entity
@Table(name = "Reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ReviewID", columnDefinition = "UNIQUEIDENTIFIER")
    String reviewId;
    
    @Column(name = "UserID", columnDefinition = "UNIQUEIDENTIFIER")
    String userId;
    
    @Column(name = "ProductID", columnDefinition = "UNIQUEIDENTIFIER") 
    String productId;
    
    @Column(name = "Rating", nullable = false)
    Integer rating; // 1-5
    
    @Column(name = "Comment", columnDefinition = "NVARCHAR(1000)")
    String comment;
    
    @Column(name = "CreatedAt", columnDefinition = "DATETIME2")
    LocalDateTime createdAt;
    
    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserID", referencedColumnName = "user_id", insertable = false, updatable = false)
    User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductID", referencedColumnName = "productID", insertable = false, updatable = false)
    Products product;
    
    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
} 