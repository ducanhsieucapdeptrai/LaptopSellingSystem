package com.example.computershop.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreateByAdmin {
    @NotBlank(message = "Tên đăng nhập không được để trống.")
    @Size(min = 3, max = 50, message = "Tên đăng nhập phải từ 3 đến 50 ký tự.")
    String username;

    @NotBlank(message = "Mật khẩu không được để trống.")
    @Size(min = 8, message = "Mật khẩu cần ít nhất 8 ký tự.")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
        message = "Mật khẩu phải chứa ít nhất 1 chữ số, 1 chữ thường, 1 chữ hoa, 1 ký tự đặc biệt và không chứa khoảng trắng."
    )
    String password;

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

    @NotNull(message = "Vai trò không được để trống.")
    String role;

    @NotNull(message = "Trạng thái không được để trống.")
    Boolean active;

    Boolean isAccountNonLocked;
}
