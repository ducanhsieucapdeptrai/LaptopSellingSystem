package com.example.computershop.controller;

import com.example.computershop.dto.request.UserCreateByAdmin;
import com.example.computershop.dto.request.UserUpdateByAdmin;
import com.example.computershop.entity.User;
import com.example.computershop.repository.UserRepository;
import com.example.computershop.service.UserService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.List;

@Controller
@RequestMapping("/admin")
@AllArgsConstructor
public class UserController {
    private UserService userService;
    private UserRepository userRepository;
    private static final String USER2 = "redirect:/admin/user";
    private static final String USER_VIEW = "admin/user/user";
    private static final String ADD_USER = "admin/user/add";
    private static final String USER = "user";
    private static final String ERROR = "error";
    private static final String MESSAGE = "message";
    private static final String EDIT_USER = "admin/user/edit";
    private static final String USER_CREATE_BY_ADMIN = "userCreateByAdmin";
    private static final String USER_UPDATE_BY_ADMIN = "userUpdateByAdmin";

    @RequestMapping("/user")
    public String showUsers(Model model) {
        List<User> users = userService.getAll();
        model.addAttribute(USER, users);
        return USER_VIEW;
    }

    @RequestMapping("/add-user")
    public String addUser(Model model) {
        model.addAttribute(USER_CREATE_BY_ADMIN, new UserCreateByAdmin());
        return ADD_USER;
    }

    @PostMapping("/add-user")
    public String addUser(@Valid @ModelAttribute("userCreateByAdmin") UserCreateByAdmin user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute(ERROR, "Vui lòng kiểm tra lại thông tin đăng ký.");
            model.addAttribute(USER_CREATE_BY_ADMIN, user);
            return ADD_USER;
        }
        try {
            userService.createUserByAdmin(user);
            model.addAttribute(MESSAGE, "Thêm người dùng thành công!");
            return USER2;
        } catch (IllegalArgumentException e) {
            model.addAttribute(ERROR, e.getMessage());
            model.addAttribute(USER_CREATE_BY_ADMIN, user);
            return ADD_USER;
        }
    }

    @GetMapping("/edit-user/{userId}")
    public String editUser(@PathVariable("userId") String userId, Model model) {
        try {
            User user = this.userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
            UserUpdateByAdmin dto = UserUpdateByAdmin.builder()
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .phoneNumber(user.getPhoneNumber())
                    .address(user.getAddress())
                    .role(user.getRole())
                    .active(user.getIsActive())
                    .isAccountLocked(user.getIsAccountLocked())
                    .build();
            model.addAttribute(USER_UPDATE_BY_ADMIN, dto);
            return EDIT_USER;
        } catch (IllegalArgumentException e) {
            model.addAttribute(ERROR, e.getMessage());
            return USER2;
        }
    }

    @PostMapping("/edit-user")
    public String updateUser(@Valid @ModelAttribute("userUpdateByAdmin") UserUpdateByAdmin user,BindingResult result,Model model) {
        if (result.hasErrors()) {
            String errorMessage = result.getFieldError() != null ? 
                result.getFieldError().getDefaultMessage() : "Thông tin không hợp lệ";
            model.addAttribute(ERROR, "Vui lòng kiểm tra lại thông tin. " + errorMessage);
            model.addAttribute(USER_UPDATE_BY_ADMIN, user);
            return EDIT_USER;
        }
        try {
            userService.updateByAdmin(user);
            model.addAttribute(MESSAGE, "Cập nhật người dùng thành công!");
            return USER2;
        } catch (IllegalArgumentException e) {
            model.addAttribute(ERROR, e.getMessage());
            model.addAttribute(USER_UPDATE_BY_ADMIN, user);
            return EDIT_USER;
        }
    }

    @PostMapping("/toggle-user-status/{userId}")
    @ResponseBody
    public String toggleUserStatus(@PathVariable("userId") String userId) {
        try {
            if (Boolean.TRUE.equals(this.userService.toggleStatus(userId))) {
                return "success";
            }
            return "error";
        } catch (Exception e) {
            return "error";
        }
    }
}
