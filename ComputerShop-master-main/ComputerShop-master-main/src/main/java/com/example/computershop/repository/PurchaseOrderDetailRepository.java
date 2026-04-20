package com.example.computershop.repository;

import com.example.computershop.entity.PurchaseOrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderDetailRepository extends JpaRepository<PurchaseOrderDetail, String> {
} 