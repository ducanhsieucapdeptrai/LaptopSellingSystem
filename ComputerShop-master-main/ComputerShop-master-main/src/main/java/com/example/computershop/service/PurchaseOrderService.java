package com.example.computershop.service;

import com.example.computershop.entity.PurchaseOrder;
import java.util.List;

public interface PurchaseOrderService {
    PurchaseOrder createPurchaseOrder(PurchaseOrder purchaseOrder);
    List<PurchaseOrder> getAllPurchaseOrders();
    PurchaseOrder getPurchaseOrderById(String id);
    void deletePurchaseOrder(String id);
} 