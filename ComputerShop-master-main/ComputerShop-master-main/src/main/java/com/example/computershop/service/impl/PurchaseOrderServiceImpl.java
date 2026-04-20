package com.example.computershop.service.impl;

import com.example.computershop.entity.PurchaseOrder;
import com.example.computershop.repository.PurchaseOrderRepository;
import com.example.computershop.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService {
    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    @Override
    public PurchaseOrder createPurchaseOrder(PurchaseOrder purchaseOrder) {
        return purchaseOrderRepository.save(purchaseOrder);
    }

    @Override
    public List<PurchaseOrder> getAllPurchaseOrders() {
        return purchaseOrderRepository.findAll();
    }

    @Override
    public PurchaseOrder getPurchaseOrderById(String id) {
        return purchaseOrderRepository.findById(id).orElse(null);
    }

    @Override
    public void deletePurchaseOrder(String id) {
        purchaseOrderRepository.deleteById(id);
    }
} 