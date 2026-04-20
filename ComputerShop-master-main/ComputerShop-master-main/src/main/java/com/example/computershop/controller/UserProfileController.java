package com.example.computershop.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.computershop.dto.UserProfileData;
import com.example.computershop.dto.request.PasswordChangeRequest;
import com.example.computershop.dto.request.UserInfoUpdateRequest;
import com.example.computershop.entity.Order;
import com.example.computershop.entity.OrderDetail;
import com.example.computershop.entity.Products;
import com.example.computershop.entity.Review;
import com.example.computershop.entity.User;
import com.example.computershop.service.OrderService;
import com.example.computershop.service.ReviewService;
import com.example.computershop.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {
    
    private final UserService userService;
    private final OrderService orderService;
    private final ReviewService reviewService;
    
    @GetMapping("/user-profile")
    public String showUserProfile(Model model) {
        try {
            // 🎯 Service xử lý tất cả logic, Controller chỉ việc display
            UserProfileData profileData = userService.getCurrentUser();
            
            // Thêm thông tin vào model
            model.addAttribute("user", profileData.getUser());
            model.addAttribute("isOAuth2", profileData.isOAuth2());
            model.addAttribute("authProvider", profileData.getAuthProvider());
            model.addAttribute("hasPhoneNumber", profileData.hasPhoneNumber());
            model.addAttribute("hasAddress", profileData.hasAddress());
            
            // Thêm empty form objects để tránh lỗi validation trong template
            model.addAttribute("userInfoUpdateRequest", new UserInfoUpdateRequest());
            model.addAttribute("passwordChangeRequest", new PasswordChangeRequest());
            
            // Thêm lịch sử đặt hàng
            List<Order> userOrders = orderService.getOrdersByUserWithDetails(profileData.getUser().getUserId());
            model.addAttribute("orders", userOrders);
            model.addAttribute("hasOrders", !userOrders.isEmpty());
            
            if (profileData.isOAuth2()) {
                model.addAttribute("oauth2Attributes", profileData.getOauth2Attributes());
                model.addAttribute("oauth2Authorities", profileData.getOauth2Authorities());
            }
            
            log.info("User profile loaded successfully for: {}", profileData.getDisplayName());
            return "user/userprofile";
            
        } catch (IllegalStateException e) {
            log.error("Authentication error: {}", e.getMessage());
            model.addAttribute("error", e.getMessage());
            return "user/userprofile";
        } catch (Exception e) {
            log.error("Unexpected error loading user profile", e);
            model.addAttribute("error", "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại.");
            return "user/userprofile";
        }
    }

    @PostMapping("/update-profile")
    public String updateProfile(@RequestParam("action") String action,
                               @ModelAttribute UserInfoUpdateRequest userInfoRequest, 
                               @ModelAttribute PasswordChangeRequest passwordRequest,
                               RedirectAttributes redirectAttributes) {
        try {
            String result;
            
            if ("update-info".equals(action)) {
                // Xử lý cập nhật thông tin cá nhân
                result = userService.updateUserInfo(userInfoRequest);
                
                if (result.equals("SUCCESS")) {
                    redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin cá nhân thành công!");
                    log.info("User info updated successfully");
                } else {
                    redirectAttributes.addFlashAttribute("error", result);
                    log.warn("User info update failed: {}", result);
                }
                
            } else if ("change-password".equals(action)) {
                // Validate password confirmation
                if (!passwordRequest.isPasswordConfirmed()) {
                    redirectAttributes.addFlashAttribute("error", "Mật khẩu xác nhận không khớp.");
                    return "redirect:/user/user-profile";
                }
                
                // Xử lý đổi mật khẩu
                result = userService.changePassword(passwordRequest);
                
                if (result.equals("SUCCESS")) {
                    redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công!");
                    log.info("Password changed successfully");
                } else {
                    redirectAttributes.addFlashAttribute("error", result);
                    log.warn("Password change failed: {}", result);
                }
                
            } else {
                redirectAttributes.addFlashAttribute("error", "Hành động không hợp lệ.");
                log.warn("Invalid action: {}", action);
            }
            
        } catch (IllegalStateException e) {
            log.error("Authentication error during profile update: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during profile update", e);
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại.");
        }
        
        return "redirect:/user/user-profile";
    }

    @PostMapping("/upload-avatar")
    public String uploadAvatar(@RequestParam("avatarFile") MultipartFile file,
                              Principal principal,
                              RedirectAttributes redirectAttributes) {
        try {
            // Check file size trước khi xử lý
            if (file.getSize() > 5 * 1024 * 1024) { // 10MB
                redirectAttributes.addFlashAttribute("error", "File quá lớn! Tối đa 10MB.");
                return "redirect:/user/user-profile";
            }
            
            User user = userService.getUserFromPrincipal(principal);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin người dùng!");
                return "redirect:/user/user-profile";
            }
            
            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Vui lòng chọn ảnh để tải lên!");
                return "redirect:/user/user-profile";
            }
            
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                redirectAttributes.addFlashAttribute("error", "File tải lên phải là ảnh!");
                return "redirect:/user/user-profile";
            }
            
            // Delete old avatar
            userService.deleteOldAvatar(user.getImage());
            
            // Save new avatar
            String avatarFileName = userService.saveAvatarFile(file, user.getUserId());
            if (avatarFileName != null) {
                String result = userService.updateUserAvatar(user.getUserId(), avatarFileName);
                if ("SUCCESS".equals(result)) {
                    redirectAttributes.addFlashAttribute("success", "✅ Tải ảnh đại diện thành công!");
                } else {
                    redirectAttributes.addFlashAttribute("error", "Lỗi khi lưu ảnh: " + result);
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi lưu ảnh!");
            }
            
        } catch (Exception e) {
            log.error("Error uploading avatar", e);
            redirectAttributes.addFlashAttribute("error", "Lỗi upload: " + e.getMessage());
        }
        
        return "redirect:/user/user-profile";
    }

    @PostMapping("/remove-avatar")
    public ResponseEntity<Map<String, Object>> removeAvatar(Principal principal) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userService.getUserFromPrincipal(principal);
            if (user == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy thông tin người dùng!");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Delete old avatar file
            userService.deleteOldAvatar(user.getImage());
            
            // Update database
            String result = userService.updateUserAvatar(user.getUserId(), null);
            if ("SUCCESS".equals(result)) {
                response.put("success", true);
                response.put("message", "Xóa ảnh đại diện thành công!");
            } else {
                response.put("success", false);
                response.put("message", "Lỗi khi cập nhật database: " + result);
            }
            
        } catch (Exception e) {
            log.error("Error removing avatar", e);
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi xóa ảnh!");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get products from order for review
     */
    @GetMapping("/order-products/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrderProducts(@PathVariable String orderId, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userService.getUserFromPrincipal(principal);
            if (user == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy thông tin người dùng!");
                return ResponseEntity.badRequest().body(response);
            }

            // Get order with details
            Order order = orderService.getOrderByIdWithDetails(orderId);
            if (order == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy đơn hàng!");
                return ResponseEntity.badRequest().body(response);
            }

            // Get products from order details and check review status
            List<Map<String, Object>> products = new ArrayList<>();
            for (OrderDetail detail : order.getOrderDetails()) {
                Products product = detail.getProduct();
                if (product != null) {
                    Map<String, Object> productInfo = new HashMap<>();
                    productInfo.put("productID", product.getProductID());
                    productInfo.put("name", product.getName());
                    productInfo.put("brand", product.getBrand());
                    productInfo.put("price", product.getPrice());
                    productInfo.put("imageURL", product.getImageURL());
                    
                    // Check if user has reviewed this product
                    Optional<Review> existingReview = reviewService.getUserReviewForProduct(user.getUserId(), product.getProductID());
                    boolean hasReviewed = existingReview.isPresent();
                    productInfo.put("hasReviewed", hasReviewed);
                    
                    // If reviewed, include review data
                    if (hasReviewed) {
                        Review review = existingReview.get();
                        Map<String, Object> reviewData = new HashMap<>();
                        reviewData.put("reviewId", review.getReviewId());
                        reviewData.put("rating", review.getRating());
                        reviewData.put("comment", review.getComment());
                        reviewData.put("createdAt", review.getCreatedAt());
                        productInfo.put("review", reviewData);
                    }
                    
                    products.add(productInfo);
                }
            }
            
            response.put("success", true);
            response.put("products", products);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error getting order products for review", e);
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi tải danh sách sản phẩm!");
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Submit product review (create or update)
     */
    @PostMapping("/submit-review")
    public ResponseEntity<Map<String, Object>> submitReview(@RequestBody Map<String, Object> request, Principal principal) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User user = userService.getUserFromPrincipal(principal);
            if (user == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy thông tin người dùng!");
                return ResponseEntity.badRequest().body(response);
            }
            
            String productId = (String) request.get("productId");
            Integer rating = (Integer) request.get("rating");
            String comment = (String) request.get("comment");
            
            // Basic validation
            if (productId == null || rating == null) {
                response.put("success", false);
                response.put("message", "Thiếu thông tin bắt buộc!");
                return ResponseEntity.badRequest().body(response);
            }

            // Check if review already exists
            Optional<Review> existingReview = reviewService.getUserReviewForProduct(user.getUserId(), productId);
            
            // Create or update review
            Review review = reviewService.UpdateReview(user.getUserId(), productId, rating, comment);
            
            response.put("success", true);
            response.put("message", existingReview.isPresent() ? 
                "Cập nhật đánh giá thành công!" : "Đánh giá sản phẩm thành công!");
            response.put("reviewId", review.getReviewId());
            response.put("isUpdate", existingReview.isPresent());
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("Error submitting review", e);
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi gửi đánh giá!");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Confirm user has received delivery - changes status from DELIVERED to USER_CONFIRMED
     */
    @PostMapping("/confirm-delivery")
    public ResponseEntity<Map<String, Object>> confirmDelivery(@RequestBody Map<String, Object> request, Principal principal) {
        Map<String, Object> response = new HashMap<>();

        try {
            User user = userService.getUserFromPrincipal(principal);
            if (user == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy thông tin người dùng!");
                return ResponseEntity.badRequest().body(response);
            }

            String orderId = (String) request.get("orderId");
            if (orderId == null || orderId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Thiếu thông tin đơn hàng!");
                return ResponseEntity.badRequest().body(response);
            }

            // Get order and validate
            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                response.put("success", false);
                response.put("message", "Không tìm thấy đơn hàng!");
                return ResponseEntity.badRequest().body(response);
            }

            // Check if order belongs to user
            if (!order.getUserId().equals(user.getUserId())) {
                response.put("success", false);
                response.put("message", "Bạn không có quyền truy cập đơn hàng này!");
                return ResponseEntity.badRequest().body(response);
            }

            // Check if order status is DELIVERED
            if (!"DELIVERED".equals(order.getStatus())) {
                response.put("success", false);
                response.put("message", "Chỉ có thể xác nhận đơn hàng đã được giao!");
                return ResponseEntity.badRequest().body(response);
            }
            String oldStatus = order.getStatus();
            // Update status to USER_CONFIRMED
            order.setStatus("USER_CONFIRMED");
            Order updatedOrder = orderService.updateOrder(order, oldStatus);

            response.put("success", true);
            response.put("message", "Xác nhận đã nhận hàng thành công! Bạn có thể đánh giá sản phẩm.");
            response.put("orderId", updatedOrder.getId());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error confirming delivery for order", e);
            response.put("success", false);
            response.put("message", "Có lỗi xảy ra khi xác nhận nhận hàng!");
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 