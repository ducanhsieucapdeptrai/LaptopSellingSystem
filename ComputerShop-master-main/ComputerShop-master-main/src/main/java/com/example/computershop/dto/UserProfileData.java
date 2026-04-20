package com.example.computershop.dto;

import com.example.computershop.entity.User;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Map;

@Data
@Builder
public class UserProfileData {
    private User user;
    private boolean isOAuth2;
    private String authProvider;
    private Map<String, Object> oauth2Attributes;
    private Collection<? extends GrantedAuthority> oauth2Authorities;
    private String realUsername; // For GitHub users with synthetic emails
    
    // Helper methods
    public boolean hasPhoneNumber() {
        return user != null && user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty();
    }
    
    public boolean hasAddress() {
        return user != null && user.getAddress() != null && !user.getAddress().isEmpty();
    }
    
    public String getDisplayName() {
        return user != null ? user.getFullName() : "Unknown User";
    }
    
    public String getRoleName() {
        return user != null ? user.getRole().name() : "UNKNOWN";
    }
    
    // GitHub specific helpers
    public boolean isGitHubWithSyntheticEmail() {
        return "github".equals(authProvider) && 
               user != null && 
               user.getEmail() != null && 
               user.getEmail().endsWith("@github.com");
    }
    
    public String getEffectiveUsername() {
        if (isGitHubWithSyntheticEmail() && realUsername != null) {
            return realUsername;
        }
        return user != null ? user.getUsername() : null;
    }
    
    public String getEffectiveEmail() {
        if (isGitHubWithSyntheticEmail()) {
            return "Email riêng tư (GitHub: " + realUsername + ")";
        }
        return user != null ? user.getEmail() : null;
    }
} 