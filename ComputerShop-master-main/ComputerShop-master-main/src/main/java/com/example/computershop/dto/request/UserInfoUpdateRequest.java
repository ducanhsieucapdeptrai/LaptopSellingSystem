package com.example.computershop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserInfoUpdateRequest {
    
    @NotBlank(message = "Họ và tên không được để trống")
    @Size(min = 2, max = 100, message = "Họ và tên phải từ 2-100 ký tự")
    private String fullName;
    
    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Số điện thoại không hợp lệ")
    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String phoneNumber;
    
    @Size(max = 500, message = "Địa chỉ tối đa 500 ký tự")
    private String address;
} 