package com.example.computershop.entity;

import com.example.computershop.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "users")
public class User implements UserDetails, OAuth2User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", columnDefinition = "UNIQUEIDENTIFIER")
    String userId;
    @Column(name = "username", unique = true, columnDefinition = "NVARCHAR(255)")
    String username;
    @Column(name = "password", columnDefinition = "NVARCHAR(255)")
    String password;
    @Column(name = "full_name", columnDefinition = "NVARCHAR(255)")
    String fullName;
    @Column(name = "email", unique = true, columnDefinition = "NVARCHAR(255)")
    String email;
    @Column(name = "phone_number", unique = true, columnDefinition = "NVARCHAR(50)")
    String phoneNumber;
    @Enumerated(EnumType.STRING)
    @Column(name = "role", columnDefinition = "NVARCHAR(50)")
    Role role;
    @Column(name = "created_at", columnDefinition = "DATETIME2")
    LocalDateTime createdAt;
    @Column(name = "is_active", columnDefinition = "BIT")
    Boolean isActive;
    @Column(name = "verification_code", columnDefinition = "NVARCHAR(255)")
    String verificationCode;
    @Column(name = "verification_expiration", columnDefinition = "DATETIME2")
    LocalDateTime verificationExpiration;
    @Column(name = "provider", columnDefinition = "NVARCHAR(255)")
    String provider;
    @Column(name = "is_account_locked", columnDefinition = "BIT")
    Boolean isAccountLocked;
    @Column(name = "address", columnDefinition = "NVARCHAR(255)")
    String address;
    @Column(name = "image", columnDefinition = "NVARCHAR(255)")
    String image;
    @Transient
    private Map<String, Object> attributes;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> "ROLE_" + capitalize(role.name()));
    }

    @Override
    public boolean isAccountNonLocked() {
        return isAccountLocked == null || !isAccountLocked;
    }

    @Override
    public boolean isEnabled() {
        return isActive != null && isActive;
    }
    @OneToMany(mappedBy = "user")
    private List<Order> orders;


    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return username;
    }

    private String capitalize(String input) {
        if (input == null || input.isEmpty()) return input;
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }
}
