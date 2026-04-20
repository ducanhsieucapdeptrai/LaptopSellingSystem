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

public class UserCreationRequest {
    @NotBlank(message = "Username cannot be empty.")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters.")
    String username;

    @NotBlank(message = "Password cannot be empty.")
    @Size(min = 8, message = "Password must be at least 8 characters.")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
        message = "Password must contain at least 1 number, 1 lowercase letter, 1 uppercase letter, 1 special character and no spaces."
    )
    String password;

    @NotBlank(message = "Confirm password cannot be empty.")
    @Size(min = 8, message = "Password must be at least 8 characters.")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
        message = "Password must contain at least 1 number, 1 lowercase letter, 1 uppercase letter, 1 special character and no spaces."
    )
    String passwordConfirm;

    @NotBlank(message = "Full name cannot be empty.")
    String fullName;

    String role = "CUSTOMER"; // Mặc định là CUSTOMER, có thể thay đổi nếu cần
    @NotBlank(message = "Email cannot be empty.")
    @Email(message = "Email is not valid.")
    String email;

    @NotBlank(message = "Phone number cannot be empty.")
    @Pattern(regexp = "^(0)(\\d{9})$", message = "Phone number must start with 0 and have 10 digits.")
    String phoneNumber;

    @NotBlank(message = "Address cannot be empty.")
    String address;
}
