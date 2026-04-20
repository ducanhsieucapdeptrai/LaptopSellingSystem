package com.example.computershop.repository;

import com.example.computershop.entity.ExportOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExportOrderRepository extends JpaRepository<ExportOrder, String> {
} 