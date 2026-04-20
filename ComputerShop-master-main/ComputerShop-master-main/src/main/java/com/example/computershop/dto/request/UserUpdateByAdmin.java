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
    @NotBlank(message = "ID cannot be empty.")
    String userId;

    @NotBlank(message = "Username cannot be empty.")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters.")
    String username;

    @NotBlank(message = "Full name cannot be empty.")
    String fullName;

    @NotBlank(message = "Email cannot be empty.")
    @Email(message = "Email is not valid.")
    String email;

    @NotBlank(message = "Phone number cannot be empty.")
    @Pattern(regexp = "^(0)(\\d{9})$", message = "Phone number must start with 0 and have 10 digits.")
    String phoneNumber;

    @NotBlank(message = "Address cannot be empty.")
    String address;

    String password; // Can be empty if not changing

    @NotNull(message = "Role cannot be empty.")
    Role role;

    @NotNull(message = "Status cannot be empty.")
    Boolean active;

    @NotNull(message = "Account locked status cannot be empty.")
    Boolean isAccountLocked;
}
