package com.example.computershop.controller;

import com.example.computershop.entity.ExportOrder;
import com.example.computershop.entity.ExportOrderDetail;
import com.example.computershop.entity.Products;
import com.example.computershop.service.ExportOrderService;
import com.example.computershop.repository.ExportOrderDetailRepository;
import com.example.computershop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/admin/export-orders")
public class ExportOrderController {
    @Autowired
    private ExportOrderService exportOrderService;
    @Autowired
    private ExportOrderDetailRepository exportOrderDetailRepository;
    @Autowired
    private ProductRepository productRepository;

    @GetMapping("")
    public String listExportOrders(Model model) {
        List<ExportOrder> orders = exportOrderService.getAllExportOrders();
        model.addAttribute("orders", orders);
        return "admin/orders/export-orders";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("order", new ExportOrder());
        model.addAttribute("products", productRepository.findAll());
        return "admin/orders/create-export-order";
    }

    @PostMapping("/create")
    public String createExportOrder(@ModelAttribute ExportOrder order,
                                    @RequestParam("productIds") List<String> productIds,
                                    @RequestParam("quantities") List<Integer> quantities,
                                    @RequestParam("exportPrices") List<Long> exportPrices,
                                    @RequestParam(value = "note", required = false) String note,
                                    @RequestParam(value = "status", required = false) String status) {
        order.setNote(note);
        order.setStatus(status != null ? status : "EXPORTED");
        ExportOrder savedOrder = exportOrderService.createExportOrder(order);
        for (int i = 0; i < productIds.size(); i++) {
            Products product = productRepository.findById(productIds.get(i)).orElse(null);
            if (product != null) {
                int qty = quantities.get(i);
                long price = exportPrices.get(i);
                // Tạo chi tiết phiếu xuất
                ExportOrderDetail detail = new ExportOrderDetail();
                detail.setProduct(product);
                detail.setQuantity(qty);
                detail.setExportPrice(java.math.BigInteger.valueOf(price));
                detail.setExportOrder(savedOrder);
                exportOrderDetailRepository.save(detail);
                // Cập nhật tồn kho
                product.setQuantity(product.getQuantity() - qty);
                productRepository.save(product);
            }
        }
        return "redirect:/admin/export-orders";
    }

    @GetMapping("/{id}")
    public String getExportOrderDetail(@PathVariable String id, Model model) {
        ExportOrder order = exportOrderService.getExportOrderById(id);
        model.addAttribute("order", order);
        return "admin/orders/export-order-detail";
    }
} 