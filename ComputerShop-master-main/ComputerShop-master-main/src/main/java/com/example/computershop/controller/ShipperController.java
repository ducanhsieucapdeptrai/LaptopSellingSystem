package com.example.computershop.controller;

import com.example.computershop.dto.UserProfileData;
import com.example.computershop.entity.Order;
import com.example.computershop.entity.User;
import com.example.computershop.enums.Role;
import com.example.computershop.service.OrderService;
import com.example.computershop.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/shipper")
@AllArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('Shipper','Admin')")
public class ShipperController {

    private final OrderService orderService;
    private final UserService userService;
    
    // Constants
    private static final String ORDERS = "orders";
    private static final String ORDER = "order";
    private static final String ERROR = "error";
    private static final String SUCCESS = "success";
    private static final String SELECTED_STATUS = "selectedStatus";
    private static final String SEARCH_TERM = "searchTerm";
    
    // View constants
    private static final String SHIPPER_DASHBOARD = "shipper/dashboard";
    private static final String SHIPPER_ORDERS_VIEW = "shipper/orders";
    private static final String SHIPPER_ORDER_DETAIL = "shipper/order-detail";
    private static final String REDIRECT_SHIPPER_ORDERS = "redirect:/shipper/orders";
    
    // Status constants for shipper operations
    private static final List<String> SHIPPER_RELEVANT_STATUSES = Arrays.asList(
        "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED"
    );

    /**
     * Hiển thị dashboard cho shipper
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        try {
            User currentUser = getCurrentUser(principal);
            
            // Lấy thống kê đơn hàng cho shipper
            List<Order> processingOrders = orderService.getOrdersByStatus("PROCESSING");
            List<Order> shippedOrders = orderService.getOrdersByStatus("SHIPPED");
            List<Order> deliveredOrders = orderService.getOrdersByStatus("DELIVERED");
            
            model.addAttribute("processingCount", processingOrders.size());
            model.addAttribute("shippedCount", shippedOrders.size());
            model.addAttribute("deliveredCount", deliveredOrders.size());
            model.addAttribute("currentUser", currentUser);
            
            return SHIPPER_DASHBOARD;
        } catch (Exception e) {
            log.error("Error loading shipper dashboard", e);
            model.addAttribute(ERROR, "Có lỗi xảy ra khi tải dashboard: " + e.getMessage());
            return SHIPPER_DASHBOARD;
        }
    }

    /**
     * Hiển thị danh sách đơn hàng cho shipper
     */
    @GetMapping("/orders")
    public String viewOrders(Model model,
                           @RequestParam(value = "status", required = false) String status,
                           @RequestParam(value = "search", required = false) String search) {
        try {
            List<Order> orders;
            
            if (status != null && !status.isEmpty()) {
                orders = orderService.getOrdersByStatus(status);
            } else {
                // Chỉ hiển thị các đơn hàng có trạng thái liên quan đến shipper
                orders = orderService.getAllOrders().stream()
                    .filter(order -> SHIPPER_RELEVANT_STATUSES.contains(order.getStatus()))
                    .collect(Collectors.toList());
            }
            
            // Lọc theo search term nếu có
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.toLowerCase().trim();
                orders = orders.stream()
                    .filter(order -> 
                        (order.getId() != null && order.getId().toLowerCase().contains(searchLower)) ||
                        (order.getCustomerName() != null && order.getCustomerName().toLowerCase().contains(searchLower)) ||
                        (order.getCustomerEmail() != null && order.getCustomerEmail().toLowerCase().contains(searchLower)) ||
                        (order.getCustomerPhone() != null && order.getCustomerPhone().toLowerCase().contains(searchLower))
                    )
                    .collect(Collectors.toList());
            }
            
            // Sắp xếp theo ngày đặt hàng mới nhất
            orders = orders.stream()
                .sorted((o1, o2) -> {
                    if (o1.getOrderDate() == null) return 1;
                    if (o2.getOrderDate() == null) return -1;
                    return o2.getOrderDate().compareTo(o1.getOrderDate());
                })
                .collect(Collectors.toList());
            
            model.addAttribute(ORDERS, orders);
            model.addAttribute("statusOptions", SHIPPER_RELEVANT_STATUSES);
            model.addAttribute(SELECTED_STATUS, status != null ? status : "");
            model.addAttribute(SEARCH_TERM, search != null ? search : "");
            model.addAttribute("totalOrders", orders.size());
            
            return SHIPPER_ORDERS_VIEW;
        } catch (Exception e) {
            log.error("Error loading orders for shipper", e);
            model.addAttribute(ERROR, "Có lỗi xảy ra khi tải danh sách đơn hàng: " + e.getMessage());
            model.addAttribute(ORDERS, List.of());
            return SHIPPER_ORDERS_VIEW;
        }
    }

    /**
     * Xem chi tiết đơn hàng
     */
    @GetMapping("/orders/{orderId}")
    public String viewOrderDetail(@PathVariable String orderId, Model model) {
        try {
            Order order = orderService.getOrderByIdWithDetails(orderId);
            if (order == null) {
                model.addAttribute(ERROR, "Không tìm thấy đơn hàng với ID: " + orderId);
                return REDIRECT_SHIPPER_ORDERS;
            }
            
            // Kiểm tra xem đơn hàng có liên quan đến shipper không
            if (!SHIPPER_RELEVANT_STATUSES.contains(order.getStatus())) {
                model.addAttribute(ERROR, "Bạn không có quyền xem đơn hàng này.");
                return REDIRECT_SHIPPER_ORDERS;
            }
            
            List<String> availableStatuses = getAvailableStatusTransitions(order.getStatus());
            
            model.addAttribute(ORDER, order);
            model.addAttribute("availableStatuses", availableStatuses);
            
            return SHIPPER_ORDER_DETAIL;
        } catch (Exception e) {
            log.error("Error loading order detail for shipper", e);
            model.addAttribute(ERROR, "Có lỗi xảy ra khi tải chi tiết đơn hàng: " + e.getMessage());
            return REDIRECT_SHIPPER_ORDERS;
        }
    }

    /**
     * Cập nhật trạng thái đơn hàng
     */
    @PostMapping("/orders/update-status")
    public String updateOrderStatus(@RequestParam String orderId,
                                  @RequestParam String newStatus,
                                  RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                redirectAttributes.addFlashAttribute(ERROR, "Không tìm thấy đơn hàng với ID: " + orderId);
                return REDIRECT_SHIPPER_ORDERS;
            }
            
            String currentStatus = order.getStatus();
            
            // Kiểm tra quyền thay đổi trạng thái
            if (!SHIPPER_RELEVANT_STATUSES.contains(currentStatus)) {
                redirectAttributes.addFlashAttribute(ERROR, "Bạn không có quyền thay đổi đơn hàng này.");
                return REDIRECT_SHIPPER_ORDERS;
            }
            
            // Kiểm tra tính hợp lệ của chuyển đổi trạng thái
            if (!isValidStatusTransition(currentStatus, newStatus)) {
                redirectAttributes.addFlashAttribute(ERROR, 
                    "Không thể chuyển từ trạng thái '" + getStatusDisplayName(currentStatus) + 
                    "' sang '" + getStatusDisplayName(newStatus) + "'");
                return "redirect:/shipper/orders/" + orderId;
            }
            String oldStatus = order.getStatus();
            // Cập nhật trạng thái
            order.setStatus(newStatus);
            orderService.updateOrder(order, oldStatus);

            log.info("Shipper updated order {} status from {} to {}", orderId, currentStatus, newStatus);
            
            redirectAttributes.addFlashAttribute(SUCCESS, 
                "Đã cập nhật trạng thái đơn hàng thành công: " + getStatusDisplayName(newStatus));
            
            return "redirect:/shipper/orders/" + orderId;
        } catch (Exception e) {
            log.error("Error updating order status", e);
            redirectAttributes.addFlashAttribute(ERROR, "Có lỗi xảy ra khi cập nhật trạng thái: " + e.getMessage());
            return REDIRECT_SHIPPER_ORDERS;
        }
    }

    /**
     * Lấy các trạng thái có thể chuyển đổi dựa trên trạng thái hiện tại
     */
    private List<String> getAvailableStatusTransitions(String currentStatus) {
        return switch (currentStatus) {
            case "PROCESSING" -> Arrays.asList("SHIPPED", "CANCELLED");
            case "SHIPPED" -> Arrays.asList("DELIVERED", "CANCELLED");
            case "DELIVERED" -> List.of(); // Không thể chuyển đổi từ DELIVERED
            case "CANCELLED" -> List.of(); // Không thể chuyển đổi từ CANCELLED
            default -> List.of();
        };
    }

    /**
     * Kiểm tra tính hợp lệ của chuyển đổi trạng thái
     */
    private boolean isValidStatusTransition(String currentStatus, String newStatus) {
        List<String> allowedTransitions = getAvailableStatusTransitions(currentStatus);
        return allowedTransitions.contains(newStatus);
    }

    /**
     * Chuyển đổi status code thành tên hiển thị tiếng Việt
     */
    private String getStatusDisplayName(String status) {
        if (status == null) return "Không xác định";
        
        return switch (status) {
            case "PROCESSING" -> "Đang xử lý";
            case "SHIPPED" -> "Đang giao hàng";
            case "DELIVERED" -> "Đã giao hàng";
            case "CANCELLED" -> "Đã hủy";
            default -> "Không xác định";
        };
    }

    /**
     * Lấy thông tin user hiện tại
     */
    private User getCurrentUser(Principal principal) {
        if (principal == null) {
            throw new IllegalStateException("Người dùng chưa đăng nhập");
        }
        
        UserProfileData userProfileData = userService.getCurrentUser();
        User user = userProfileData.getUser();

        if (user.getRole() == Role.Shipper || user.getRole() == Role.Admin) {
            return user;
        }else{
            throw new IllegalStateException("Quản trị viên hoặc Nhân viên vận chuyển mới có thể truy cập trang này");
        }
    }
} 