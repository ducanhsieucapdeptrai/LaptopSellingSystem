package com.example.computershop.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Entity
@Table(name = "export_order")
public class ExportOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", unique = true, nullable = false, columnDefinition = "nvarchar(255)")
    String id;

    @Column(name = "created_at", columnDefinition = "datetime")
    LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "note", columnDefinition = "nvarchar(255)")
    String note;

    @Column(name = "status", columnDefinition = "nvarchar(50)")
    String status; // EXPORTED, PENDING

    @OneToMany(mappedBy = "exportOrder", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<ExportOrderDetail> details;
} 