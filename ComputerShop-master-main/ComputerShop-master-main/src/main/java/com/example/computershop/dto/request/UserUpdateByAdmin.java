package com.example.computershop.dto.request;

import com.example.computershop.enums.Role;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateByAdmin {
    @NotBlank(message = "ID không được để trống.")
    String userId;

    @NotBlank(message = "Tên đăng nhập không được để trống.")
    @Size(min = 3, max = 50, message = "Tên đăng nhập phải từ 3 đến 50 ký tự.")
    String username;

    @NotBlank(message = "Họ tên không được để trống.")
    String fullName;

    @NotBlank(message = "Email không được để trống.")
    @Email(message = "Email không hợp lệ.")
    String email;

    @NotBlank(message = "Số điện thoại không được để trống.")
    @Pattern(regexp = "^(0)(\\d{9})$", message = "Số điện thoại phải bắt đầu bằng số 0 và có 10 chữ số.")
    String phoneNumber;

    @NotBlank(message = "Địa chỉ không được để trống.")
    String address;

    String password; // Có thể để trống nếu không đổi

    @NotNull(message = "Vai trò không được để trống.")
    Role role;

    @NotNull(message = "Trạng thái không được để trống.")
    Boolean active;

    @NotNull(message = "Trạng thái khóa tài khoản không được để trống.")
    Boolean isAccountLocked;
}
