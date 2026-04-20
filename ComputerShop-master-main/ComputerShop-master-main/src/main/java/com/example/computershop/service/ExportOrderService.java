package com.example.computershop.service;

import com.example.computershop.entity.ExportOrder;
import java.util.List;

public interface ExportOrderService {
    ExportOrder createExportOrder(ExportOrder exportOrder);
    List<ExportOrder> getAllExportOrders();
    ExportOrder getExportOrderById(String id);
    void deleteExportOrder(String id);
} 