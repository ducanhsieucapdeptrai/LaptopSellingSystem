package com.example.computershop.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserInfoUpdateRequest {
    
    @NotBlank(message = "Full name cannot be empty.")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters.")
    private String fullName;
    
    @Pattern(regexp = "^[0-9+\\-\\s()]*$", message = "Phone number is not valid.")
    @Size(max = 20, message = "Phone number must be at most 20 characters.")
    private String phoneNumber;
    
    @Size(max = 500, message = "Address must be at most 500 characters.")
    private String address;
} 