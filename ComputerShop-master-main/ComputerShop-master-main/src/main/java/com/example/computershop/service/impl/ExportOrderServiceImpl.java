package com.example.computershop.service.impl;

import com.example.computershop.entity.ExportOrder;
import com.example.computershop.repository.ExportOrderRepository;
import com.example.computershop.service.ExportOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ExportOrderServiceImpl implements ExportOrderService {
    @Autowired
    private ExportOrderRepository exportOrderRepository;

    @Override
    public ExportOrder createExportOrder(ExportOrder exportOrder) {
        return exportOrderRepository.save(exportOrder);
    }

    @Override
    public List<ExportOrder> getAllExportOrders() {
        return exportOrderRepository.findAll();
    }

    @Override
    public ExportOrder getExportOrderById(String id) {
        return exportOrderRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteExportOrder(String id) {
        exportOrderRepository.deleteById(id);
    }
} 