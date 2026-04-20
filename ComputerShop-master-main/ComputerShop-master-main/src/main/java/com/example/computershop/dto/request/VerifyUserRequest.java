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
    @NotBlank(message = "Email cannot be empty.")
    @Email(message = "Email is not valid.")
    String email;
    String verificationCode;
}
