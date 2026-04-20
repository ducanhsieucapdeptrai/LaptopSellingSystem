package com.example.computershop.controller;

import com.example.computershop.entity.Notification;
import com.example.computershop.entity.User;
import com.example.computershop.service.NotificationService;
import com.example.computershop.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @Autowired
    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    // ===== USER ENDPOINTS =====

    @GetMapping("/user")
    public String userNotifications(Model model, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) {
            return "redirect:/auth/login";
        }

        List<Notification> userNotifications = notificationService.getNotificationsByUserId(user.getUserId());
        long unreadCount = notificationService.countUnreadNotificationsByUserId(user.getUserId());

        model.addAttribute("notifications", userNotifications);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("user", user);

        return "user/notifications";
    }

    @GetMapping("/api/unread-count")
    @ResponseBody
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getUnreadCount(Principal principal) {
        try {
            User user = getCurrentUser(principal);
            if (user == null) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "User not authenticated");
                errorResponse.put("success", false);
                errorResponse.put("unreadCount", 0);
                return ResponseEntity.ok(errorResponse);
            }

            long unreadCount = notificationService.countUnreadNotificationsByUserId(user.getUserId());

            Map<String, Object> response = new HashMap<>();
            response.put("unreadCount", unreadCount);
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error");
            errorResponse.put("success", false);
            errorResponse.put("unreadCount", 0);
            return ResponseEntity.ok(errorResponse);
        }
    }

    @GetMapping("/api/user-notifications")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserNotifications(Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
        }

        List<Notification> userNotifications = notificationService.getNotificationsByUserId(user.getUserId());

        Map<String, Object> response = new HashMap<>();
        response.put("notifications", userNotifications);
        response.put("success", true);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/unread-notifications")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUnreadNotifications(Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
        }

        List<Notification> unreadNotifications = notificationService.getUnreadNotificationsByUserId(user.getUserId());

        Map<String, Object> response = new HashMap<>();
        response.put("notifications", unreadNotifications);
        response.put("unreadCount", unreadNotifications.size());
        response.put("success", true);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/mark-read/{notificationId}")
    public String markAsReadWeb(@PathVariable String notificationId,
                                @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
                                Principal principal, RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(principal);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "You need to log in");
            return "redirect:/auth/login";
        }

        try {
            Notification notification = notificationService.getNotificationById(notificationId);
            if (notification == null) {
                redirectAttributes.addFlashAttribute("error", "Notification not found");
                return redirectUrl != null ? "redirect:" + redirectUrl : "redirect:/notifications/admin";
            }

            if (!notification.getUserId().equals(user.getUserId())) {
                redirectAttributes.addFlashAttribute("error", "You do not have permission to perform this action");
                return redirectUrl != null ? "redirect:" + redirectUrl : "redirect:/notifications/admin";
            }

            notificationService.markNotificationAsRead(notificationId);
            redirectAttributes.addFlashAttribute("success", "Notification marked as read");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred: " + e.getMessage());
        }

        return redirectUrl != null ? "redirect:" + redirectUrl : "redirect:/notifications/admin";
    }

    @PostMapping("/api/mark-read/{notificationId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable String notificationId, Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
        }

        Notification notification = notificationService.getNotificationById(notificationId);
        if (notification == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Notification not found"));
        }

        if (!notification.getUserId().equals(user.getUserId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Access denied"));
        }

        notificationService.markNotificationAsRead(notificationId);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Notification marked as read");
        response.put("success", true);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/mark-all-read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAllAsRead(Principal principal) {
        User user = getCurrentUser(principal);
        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not authenticated"));
        }

        notificationService.markAllNotificationsAsRead(user.getUserId());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "All notifications marked as read");
        response.put("success", true);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/broadcast")
    @PreAuthorize("hasRole('Admin')")
    public String showBroadcastForm(Model model) {
        long totalUsers = userService.getAll().size();
        long onlineUsers = 0;

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("onlineUsers", onlineUsers);

        return "admin/notifications/broadcast";
    }

    @PostMapping("/admin/broadcast")
    @PreAuthorize("hasRole('Admin')")
    public String sendBroadcastNotification(@RequestParam("message") String message,
                                            RedirectAttributes redirectAttributes) {
        try {
            if (message == null || message.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Message content cannot be empty");
                return "redirect:/notifications/admin/broadcast";
            }

            notificationService.createBroadcastNotification(message.trim());
            redirectAttributes.addFlashAttribute("success", "Notification sent to all users successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred while sending notification: " + e.getMessage());
        }

        return "redirect:/notifications/admin";
    }

    @GetMapping("/admin/detail/{notificationId}")
    public String viewNotificationDetail(@PathVariable String notificationId, Model model,
                                         Principal principal, RedirectAttributes redirectAttributes) {
        try {
            User admin = getCurrentUser(principal);
            if (admin == null) {
                redirectAttributes.addFlashAttribute("error", "You need to log in to access this page");
                return "redirect:/auth/login";
            }

            Notification notification = notificationService.getNotificationById(notificationId);
            if (notification == null) {
                redirectAttributes.addFlashAttribute("error", "Notification not found");
                return "redirect:/notifications/admin";
            }

            Map<String, Object> relatedInfo = getDetailedRelatedInfo(notification);

            if (notification.getIsRead() == null || !notification.getIsRead()) {
                notificationService.markNotificationAsRead(notificationId);
                notification = notificationService.getNotificationById(notificationId);
            }

            model.addAttribute("notification", notification);
            model.addAttribute("relatedInfo", relatedInfo);

            return "admin/notifications/detail";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An error occurred while loading data: " + e.getMessage());
            return "redirect:/notifications/admin";
        }
    }

    private Map<String, Object> getDetailedRelatedInfo(Notification notification) {
        Map<String, Object> relatedInfo = new HashMap<>();

        try {
            relatedInfo.put("notificationId", notification.getNotificationId());
            relatedInfo.put("userId", notification.getUserId());
            relatedInfo.put("createdAt", notification.getCreatedAt());
            relatedInfo.put("isRead", notification.getIsRead());

            if (notification.getOrderId() != null) {
                relatedInfo.put("type", "order");
                relatedInfo.put("typeDescription", "Order Notification");
            } else if (notification.getProductId() != null) {
                relatedInfo.put("type", "product");
                relatedInfo.put("typeDescription", "Product Notification");
            } else {
                relatedInfo.put("type", "system");
                relatedInfo.put("typeDescription", "System Notification");
                relatedInfo.put("systemInfo", "General system notification");
            }

        } catch (Exception e) {
            relatedInfo.put("error", "Unable to load related information: " + e.getMessage());
        }

        return relatedInfo;
    }

    private User getCurrentUser(Principal principal) {
        if (principal == null) return null;
        try {
            return userService.getUserFromPrincipal(principal);
        } catch (Exception e) {
            return null;
        }
    }
}