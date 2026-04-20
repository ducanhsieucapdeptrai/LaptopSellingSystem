package com.example.computershop.service;

import com.example.computershop.entity.PurchaseOrderDetail;

public interface PurchaseOrderDetailService {
    PurchaseOrderDetail addDetail(PurchaseOrderDetail detail);
    PurchaseOrderDetail getDetailById(String id);
    void deleteDetail(String id);
} 