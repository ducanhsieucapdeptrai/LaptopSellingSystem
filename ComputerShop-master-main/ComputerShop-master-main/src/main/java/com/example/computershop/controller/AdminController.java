package com.example.computershop.controller;

import com.example.computershop.dto.ProductSalesDTO;
import com.example.computershop.entity.Products;
import com.example.computershop.service.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Year;
import java.util.List;

@RequestMapping("/admin")
@AllArgsConstructor
@Controller
public class AdminController {

    private OrderService orderService;
    private ProductService productService;
    private UserService userService;

    @RequestMapping("/dashboard")
    public String showAdminPage(@RequestParam(required = false) Integer year, Model model){
        model.addAttribute("userCount", userService.countUsers());
        model.addAttribute("productCount", productService.countProducts());
        model.addAttribute("orderCount", orderService.countOrders());
        model.addAttribute("revenue", orderService.getTotalRevenue());

        if (year == null) {
            year = Year.now().getValue(); // Mặc định là năm hiện tại
        }
        List<Object[]> results = orderService.getMonthlyRevenue(year);

        // Mảng 12 tháng, mặc định doanh thu là 0
        long[] monthlyRevenue = new long[12];
        for (Object[] row : results) {
            int month = (int) row[0]; // từ 1 đến 12
            long revenue = (long) row[1];
            monthlyRevenue[month - 1] = revenue;
        }

        // Lấy danh sách các năm có đơn hàng
        List<Integer> years = orderService.getDistinctYears();

        List<Products> topStockProducts = productService.findTop5ProductsByStock();
        model.addAttribute("topStockProducts", topStockProducts);

        // Lấy top 5 sản phẩm bán chạy nhất
        List<ProductSalesDTO> topProducts = productService.findTop5BestSellingProducts();
        model.addAttribute("topProducts", topProducts);

        List<Object[]> stats = userService.getTop5UserByRevenueOrder();
        model.addAttribute("userStats", stats);

        model.addAttribute("monthlyRevenue", monthlyRevenue);
        model.addAttribute("selectedYear", year);
        model.addAttribute("years", years);
        return "admin/dashboard";
    }
}
