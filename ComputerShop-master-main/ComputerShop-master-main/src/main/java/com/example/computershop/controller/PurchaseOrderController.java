package com.example.computershop.controller;

import com.example.computershop.entity.PurchaseOrder;
import com.example.computershop.entity.PurchaseOrderDetail;
import com.example.computershop.entity.Products;
import com.example.computershop.service.PurchaseOrderService;
import com.example.computershop.service.PurchaseOrderDetailService;
import com.example.computershop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/purchase-orders")
public class PurchaseOrderController {
    @Autowired
    private PurchaseOrderService purchaseOrderService;
    @Autowired
    private PurchaseOrderDetailService purchaseOrderDetailService;
    @Autowired
    private ProductRepository productRepository;

    @GetMapping("")
    public String listPurchaseOrders(
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate toDate,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "debt", required = false) String debt,
            Model model) {
        List<PurchaseOrder> orders = purchaseOrderService.getAllPurchaseOrders();
        // Lọc theo ngày
        if (fromDate != null) {
            orders = orders.stream().filter(o -> o.getCreatedAt().toLocalDate().compareTo(fromDate) >= 0).toList();
        }
        if (toDate != null) {
            orders = orders.stream().filter(o -> o.getCreatedAt().toLocalDate().compareTo(toDate) <= 0).toList();
        }
        // Lọc theo trạng thái
        if (status != null && !status.isEmpty()) {
            orders = orders.stream().filter(o -> status.equals(o.getStatus())).toList();
        }
        // Lọc theo công nợ
        if (debt != null && !debt.isEmpty()) {
            if (debt.equals("debt")) {
                orders = orders.stream().filter(o -> o.getDebtAmount() != null && o.getDebtAmount().longValue() > 0).toList();
            } else if (debt.equals("paid")) {
                orders = orders.stream().filter(o -> o.getDebtAmount() == null || o.getDebtAmount().longValue() == 0).toList();
            }
        }
        model.addAttribute("orders", orders);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("status", status);
        model.addAttribute("debt", debt);
        return "admin/orders/purchase-orders";
    }

    @GetMapping("/{id}")
    public String getPurchaseOrderDetail(@PathVariable String id, Model model) {
        PurchaseOrder order = purchaseOrderService.getPurchaseOrderById(id);
        model.addAttribute("order", order);
        return "admin/orders/purchase-order-detail";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("order", new PurchaseOrder());
        model.addAttribute("products", productRepository.findAll());
        return "admin/orders/create-purchase-order";
    }

    @PostMapping("/create")
    public String createPurchaseOrder(@ModelAttribute PurchaseOrder order,
                                 @RequestParam("productIds") List<String> productIds,
                                 @RequestParam("quantities") List<Integer> quantities,
                                 @RequestParam("importPrices") List<Long> importPrices,
                                 @RequestParam(value = "paidAmount", required = false) Long paidAmount,
                                 @RequestParam(value = "status", required = false) String status) {
        // Tính tổng tiền
        java.math.BigInteger total = java.math.BigInteger.ZERO;
        for (int i = 0; i < productIds.size(); i++) {
            int qty = quantities.get(i);
            long price = importPrices.get(i);
            total = total.add(java.math.BigInteger.valueOf(qty).multiply(java.math.BigInteger.valueOf(price)));
        }
        order.setTotalAmount(total);
        // Xử lý logic phần còn nợ và trạng thái
        java.math.BigInteger paid = paidAmount != null ? java.math.BigInteger.valueOf(paidAmount) : java.math.BigInteger.ZERO;
        java.math.BigInteger debt = total.subtract(paid);
        if (debt.compareTo(java.math.BigInteger.ZERO) <= 0) {
            order.setStatus("PAID");
            order.setDebtAmount(java.math.BigInteger.ZERO);
            order.setPaidAmount(total);
        } else {
            order.setDebtAmount(debt);
            order.setPaidAmount(paid);
            if (paid.equals(java.math.BigInteger.ZERO)) {
                order.setStatus("UNPAID");
            } else {
                order.setStatus("PARTIAL");
            }
        }
        // Lưu phiếu nhập
        PurchaseOrder savedOrder = purchaseOrderService.createPurchaseOrder(order);
        // Lưu chi tiết phiếu nhập và cập nhật tồn kho
        for (int i = 0; i < productIds.size(); i++) {
            Products product = productRepository.findById(productIds.get(i)).orElse(null);
            if (product != null) {
                int qty = quantities.get(i);
                long price = importPrices.get(i);
                // Tạo chi tiết phiếu nhập
                PurchaseOrderDetail detail = new PurchaseOrderDetail();
                detail.setProduct(product);
                detail.setQuantity(qty);
                detail.setImportPrice(java.math.BigInteger.valueOf(price));
                detail.setPurchaseOrder(savedOrder);
                purchaseOrderDetailService.addDetail(detail);
                // Cập nhật tồn kho
                product.setQuantity(product.getQuantity() + qty);
                productRepository.save(product);
            }
        }
        return "redirect:/admin/purchase-orders";
    }

    @GetMapping("/{id}/add-detail")
    public String showAddDetailForm(@PathVariable String id, Model model) {
        PurchaseOrder order = purchaseOrderService.getPurchaseOrderById(id);
        model.addAttribute("order", order);
        model.addAttribute("detail", new PurchaseOrderDetail());
        model.addAttribute("products", productRepository.findAll());
        return "admin/orders/add-purchase-order-detail";
    }

    @PostMapping("/{id}/add-detail")
    public String addDetail(@PathVariable String id, @ModelAttribute PurchaseOrderDetail detail) {
        PurchaseOrder order = purchaseOrderService.getPurchaseOrderById(id);
        detail.setPurchaseOrder(order);
        // Cập nhật tồn kho sản phẩm
        Products product = detail.getProduct();
        Products dbProduct = productRepository.findById(product.getProductID()).orElse(null);
        if (dbProduct != null) {
            dbProduct.setQuantity(dbProduct.getQuantity() + detail.getQuantity());
            productRepository.save(dbProduct);
        }
        purchaseOrderDetailService.addDetail(detail);
        return "redirect:/admin/purchase-orders/" + id;
    }

    @GetMapping("/detail/{detailId}/edit")
    public String showEditDetailForm(@PathVariable String detailId, Model model) {
        PurchaseOrderDetail detail = purchaseOrderDetailService.getDetailById(detailId);
        model.addAttribute("detail", detail);
        model.addAttribute("order", detail.getPurchaseOrder());
        model.addAttribute("products", productRepository.findAll());
        return "admin/orders/edit-purchase-order-detail";
    }

    @PostMapping("/detail/{detailId}/edit")
    public String editDetail(@PathVariable String detailId, @ModelAttribute PurchaseOrderDetail updatedDetail) {
        PurchaseOrderDetail oldDetail = purchaseOrderDetailService.getDetailById(detailId);
        Products product = oldDetail.getProduct();
        // Trừ tồn kho cũ
        product.setQuantity(product.getQuantity() - oldDetail.getQuantity());
        // Cộng tồn kho mới
        product.setQuantity(product.getQuantity() + updatedDetail.getQuantity());
        productRepository.save(product);
        // Cập nhật thông tin chi tiết
        oldDetail.setQuantity(updatedDetail.getQuantity());
        oldDetail.setImportPrice(updatedDetail.getImportPrice());
        purchaseOrderDetailService.addDetail(oldDetail);
        return "redirect:/admin/purchase-orders/" + oldDetail.getPurchaseOrder().getId();
    }

    @GetMapping("/detail/{detailId}/delete")
    public String deleteDetail(@PathVariable String detailId) {
        PurchaseOrderDetail detail = purchaseOrderDetailService.getDetailById(detailId);
        Products product = detail.getProduct();
        // Trừ tồn kho sản phẩm
        product.setQuantity(product.getQuantity() - detail.getQuantity());
        productRepository.save(product);
        String orderId = detail.getPurchaseOrder().getId();
        purchaseOrderDetailService.deleteDetail(detailId);
        return "redirect:/admin/purchase-orders/" + orderId;
    }

    @GetMapping("/{id}/delete")
    public String deletePurchaseOrder(@PathVariable String id) {
        PurchaseOrder order = purchaseOrderService.getPurchaseOrderById(id);
        if (order != null && order.getDetails() != null) {
            for (PurchaseOrderDetail detail : order.getDetails()) {
                Products product = detail.getProduct();
                product.setQuantity(product.getQuantity() - detail.getQuantity());
                productRepository.save(product);
            }
        }
        purchaseOrderService.deletePurchaseOrder(id);
        return "redirect:/admin/purchase-orders";
    }
} 