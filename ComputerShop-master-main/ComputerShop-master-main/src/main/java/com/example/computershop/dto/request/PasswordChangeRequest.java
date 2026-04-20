package com.example.computershop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordChangeRequest {
    
    @NotBlank(message = "Current password cannot be empty")
    private String currentPassword;
    
    @NotBlank(message = "New password cannot be empty")
    @Size(min = 6, max = 100, message = "New password must be between 6 and 100 characters")
    private String newPassword;
    
    @NotBlank(message = "Confirm password cannot be empty")
    private String confirmPassword;
    
    // Helper method to validate password confirmation
    public boolean isPasswordConfirmed() {
        return newPassword == null || !newPassword.equals(confirmPassword);
    }
} 