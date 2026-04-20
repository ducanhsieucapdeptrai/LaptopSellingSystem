package com.example.computershop.controller;

import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.computershop.service.OrderService;
import com.example.computershop.service.ProductService;
import com.example.computershop.service.UserService;
import com.example.computershop.dto.ProductSalesDTO;

@RestController
@RequestMapping("/admin/api")
public class StatisticsApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        try {
            // Basic counts
            Long userCount = null;
            try {
                userCount = userService.countUsers();
            } catch (Exception e) {
                System.err.println("Lỗi lấy tổng số người dùng: " + e.getMessage());
            }
            statistics.put("userCount", userCount != null ? userCount : 0L);

            Long productCount = null;
            try {
                productCount = productService.countProducts();
            } catch (Exception e) {
                System.err.println("Lỗi lấy tổng số sản phẩm: " + e.getMessage());
            }
            statistics.put("productCount", productCount != null ? productCount : 0L);

            Long orderCount = null;
            try {
                orderCount = orderService.countOrders();
            } catch (Exception e) {
                System.err.println("Lỗi lấy tổng số đơn hàng: " + e.getMessage());
            }
            statistics.put("orderCount", orderCount != null ? orderCount : 0L);

            Object revenue = null;
            try {
                revenue = orderService.getTotalRevenue();
            } catch (Exception e) {
                System.err.println("Lỗi lấy tổng doanh thu: " + e.getMessage());
            }
            statistics.put("revenue", revenue != null ? revenue : 0);

            // Monthly revenue for current year
            int currentYear = Year.now().getValue();
            List<Object[]> results = new ArrayList<>();
            try {
                List<Object[]> temp = orderService.getMonthlyRevenue(currentYear);
                if (temp != null) results = temp;
            } catch (Exception e) {
                System.err.println("Lỗi lấy doanh thu theo tháng: " + e.getMessage());
            }
            long[] monthlyRevenue = new long[12];
            for (Object[] row : results) {
                if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                    int month = (int) row[0];
                    long rev = ((Number) row[1]).longValue();
                    monthlyRevenue[month - 1] = rev;
                }
            }
            statistics.put("monthlyRevenue", monthlyRevenue);

            // Số lượng đơn hàng theo tháng
            long[] orderCountByMonth = new long[12];
            List<Object[]> orderCountResults = new ArrayList<>();
            try {
                List<Object[]> temp = orderService.getMonthlyOrderCount(currentYear);
                if (temp != null) orderCountResults = temp;
            } catch (Exception e) {
                System.err.println("Lỗi lấy số lượng đơn hàng theo tháng: " + e.getMessage());
            }
            for (Object[] row : orderCountResults) {
                if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                    int month = (int) row[0];
                    long count = ((Number) row[1]).longValue();
                    orderCountByMonth[month - 1] = count;
                }
            }
            statistics.put("orderCountByMonth", orderCountByMonth);

            // Số lượng user mới theo tháng
            long[] newUsersByMonth = new long[12];
            List<Object[]> newUserResults = new ArrayList<>();
            try {
                List<Object[]> temp = userService.getMonthlyNewUsers(currentYear);
                if (temp != null) newUserResults = temp;
            } catch (Exception e) {
                System.err.println("Lỗi lấy số lượng user mới theo tháng: " + e.getMessage());
            }
            for (Object[] row : newUserResults) {
                if (row != null && row.length >= 2 && row[0] != null && row[1] != null) {
                    int month = (int) row[0];
                    long count = ((Number) row[1]).longValue();
                    newUsersByMonth[month - 1] = count;
                }
            }
            statistics.put("newUsersByMonth", newUsersByMonth);

            // Lấy top 5 sản phẩm bán chạy nhất từ DB
            List<ProductSalesDTO> topProductDTOs = new ArrayList<>();
            try {
                List<ProductSalesDTO> temp = productService.findTop5BestSellingProducts();
                if (temp != null) topProductDTOs = temp;
            } catch (Exception e) {
                System.err.println("Lỗi lấy top sản phẩm bán chạy: " + e.getMessage());
            }
            List<Map<String, Object>> topProducts = new ArrayList<>();
            for (ProductSalesDTO dto : topProductDTOs) {
                if (dto != null && dto.getProduct() != null) {
                    Map<String, Object> prod = new HashMap<>();
                    prod.put("name", dto.getProduct().getName());
                    prod.put("imageURL", dto.getProduct().getImageURL());
                    prod.put("price", dto.getProduct().getPrice());
                    prod.put("totalSold", dto.getTotalSold());
                    prod.put("totalRevenue", dto.getTotalRevenue());
                    topProducts.add(prod);
                }
            }
            statistics.put("topProducts", topProducts);

            // Giá trị đơn hàng trung bình
            statistics.put("averageOrderValue", 0);
            // Số khách hàng đã mua
            statistics.put("purchasedCustomerCount", 0);
            // Tỷ lệ chuyển đổi
            statistics.put("conversionRate", 0);

            return ResponseEntity.ok(statistics);

        } catch (Exception e) {
            e.printStackTrace(); // Log chi tiết lỗi
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to load statistics");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/statistics/panel")
    public String getStatisticsPanel() {
        return "admin/fragments/statistics-panel :: statistics-panel";
    }
}
