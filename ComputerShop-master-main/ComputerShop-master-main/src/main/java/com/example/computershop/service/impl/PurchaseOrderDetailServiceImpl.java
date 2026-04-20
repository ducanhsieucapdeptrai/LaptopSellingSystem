package com.example.computershop.service.impl;

import com.example.computershop.entity.PurchaseOrderDetail;
import com.example.computershop.repository.PurchaseOrderDetailRepository;
import com.example.computershop.service.PurchaseOrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PurchaseOrderDetailServiceImpl implements PurchaseOrderDetailService {
    @Autowired
    private PurchaseOrderDetailRepository detailRepository;

    @Override
    public PurchaseOrderDetail addDetail(PurchaseOrderDetail detail) {
        return detailRepository.save(detail);
    }

    @Override
    public PurchaseOrderDetail getDetailById(String id) {
        return detailRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteDetail(String id) {
        detailRepository.deleteById(id);
    }
} 