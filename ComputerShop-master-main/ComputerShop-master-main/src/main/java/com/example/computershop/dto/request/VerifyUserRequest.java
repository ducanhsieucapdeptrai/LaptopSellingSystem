package com.example.computershop.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyUserRequest {
    @NotBlank(message = "Email không được để trống.")
    @Email(message = "Email không hợp lệ.")
    String email;
    String verificationCode;
}
