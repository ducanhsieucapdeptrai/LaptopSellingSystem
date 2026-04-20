package com.example.computershop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.Year;
import java.util.List;
import com.example.computershop.service.OrderService;
import com.example.computershop.service.ProductService;
import com.example.computershop.service.UserService;
import com.example.computershop.dto.ProductSalesDTO;

@RequestMapping("/admin/reports")
@Controller
public class SimpleReportController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductService productService;
    @Autowired
    private UserService userService;

    @GetMapping("/statistics")
    public String showStatistics(Model model) {
        
        // Dữ liệu mẫu cho demo
        model.addAttribute("totalUsers", 150);
        model.addAttribute("totalProducts", 45);
        model.addAttribute("totalOrders", 320);
        model.addAttribute("totalRevenue", 125000000L);

        // Dữ liệu doanh thu theo tháng (mẫu)
        long[] monthlyRevenue = {
            8500000L, 9200000L, 10100000L, 11500000L, 
            12800000L, 14200000L, 13900000L, 15600000L,
            16800000L, 18200000L, 19500000L, 21000000L
        };
        model.addAttribute("monthlyRevenue", monthlyRevenue);
        model.addAttribute("selectedYear", 2024);

        // Thống kê theo khoảng thời gian (mẫu)
        model.addAttribute("periodStats", java.util.Map.of(
            "ordersInPeriod", 45L,
            "revenueInPeriod", 15600000L,
            "newCustomers", 12L,
            "avgOrdersPerDay", 1.5
        ));
        model.addAttribute("startDate", java.time.LocalDate.now().minusDays(30));
        model.addAttribute("endDate", java.time.LocalDate.now());

        // Top sản phẩm bán chạy (mẫu)
        java.util.List<java.util.Map<String, Object>> topProducts = java.util.List.of(
            java.util.Map.of("name", "Laptop Dell XPS 13", "totalSold", 25L, "price", 25000000L),
            java.util.Map.of("name", "MacBook Air M2", "totalSold", 18L, "price", 28000000L),
            java.util.Map.of("name", "PC Gaming RTX 4070", "totalSold", 15L, "price", 45000000L),
            java.util.Map.of("name", "Laptop HP Pavilion", "totalSold", 12L, "price", 18000000L),
            java.util.Map.of("name", "Laptop Asus ROG", "totalSold", 8L, "price", 35000000L)
        );
        model.addAttribute("topProducts", topProducts);

        // Top khách hàng VIP (mẫu)
        java.util.List<Object[]> topCustomers = java.util.List.of(
            new Object[]{1L, "Nguyễn Văn A", 8L, 45000000L},
            new Object[]{2L, "Trần Thị B", 6L, 38000000L},
            new Object[]{3L, "Lê Văn C", 5L, 32000000L},
            new Object[]{4L, "Phạm Thị D", 4L, 28000000L},
            new Object[]{5L, "Hoàng Văn E", 3L, 25000000L}
        );
        model.addAttribute("topCustomers", topCustomers);

        // Sản phẩm sắp hết hàng (mẫu)
        java.util.List<java.util.Map<String, Object>> lowStockProducts = java.util.List.of(
            java.util.Map.of("name", "Laptop Dell XPS 13", "quantity", 3),
            java.util.Map.of("name", "PC Gaming RTX 4070", "quantity", 2),
            java.util.Map.of("name", "MacBook Pro 16", "quantity", 1),
            java.util.Map.of("name", "Gaming Chair", "quantity", 4),
            java.util.Map.of("name", "Mechanical Keyboard", "quantity", 2)
        );
        model.addAttribute("lowStockProducts", lowStockProducts);

        // Thống kê đơn hàng theo trạng thái (mẫu)
        java.util.Map<String, Long> orderStatusStats = java.util.Map.of(
            "Completed", 280L,
            "Processing", 25L,
            "Shipped", 15L,
            "Cancelled", 8L,
            "Pending", 12L
        );
        model.addAttribute("orderStatusStats", orderStatusStats);

        // Danh sách các năm có dữ liệu (mẫu)
        java.util.List<Integer> years = java.util.List.of(2022, 2023, 2024);
        model.addAttribute("years", years);

        return "admin/reports/statistics";
    }

    @GetMapping("/sales")
    public String showSalesReport(Model model) {
        int year = Year.now().getValue();
        List<Object[]> results = orderService.getMonthlyRevenue(year);
        long[] monthlyRevenue = new long[12];
        for (Object[] row : results) {
            int month = (int) row[0];
            long rev = ((Number) row[1]).longValue();
            monthlyRevenue[month - 1] = rev;
        }
        model.addAttribute("monthlyRevenue", monthlyRevenue);
        return "admin/reports/sales";
    }

    @GetMapping("/products")
    public String showProductReport(Model model) {
        List<ProductSalesDTO> topProductDTOs = productService.findTop5BestSellingProducts();
        List<java.util.Map<String, Object>> topProducts = new java.util.ArrayList<>();
        for (ProductSalesDTO dto : topProductDTOs) {
            if (dto != null && dto.getProduct() != null) {
                java.util.Map<String, Object> prod = new java.util.HashMap<>();
                prod.put("name", dto.getProduct().getName());
                prod.put("imageURL", dto.getProduct().getImageURL());
                prod.put("totalSold", dto.getTotalSold());
                prod.put("totalRevenue", dto.getTotalRevenue());
                topProducts.add(prod);
            }
        }
        model.addAttribute("topProducts", topProducts);
        return "admin/reports/products";
    }

    @GetMapping("/customers")
    public String showCustomerReport(Model model) {
        int year = Year.now().getValue();
        List<Object[]> results = userService.getMonthlyNewUsers(year);
        long[] newUsersByMonth = new long[12];
        for (Object[] row : results) {
            int month = (int) row[0];
            long count = ((Number) row[1]).longValue();
            newUsersByMonth[month - 1] = count;
        }
        model.addAttribute("newUsersByMonth", newUsersByMonth);
        return "admin/reports/customers";
    }
}
