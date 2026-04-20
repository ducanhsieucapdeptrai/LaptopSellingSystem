package com.example.computershop.service;

import com.example.computershop.dto.request.UserCreationRequest;
import com.example.computershop.dto.request.VerifyUserRequest;
import com.example.computershop.entity.User;
import com.example.computershop.enums.Role;
import com.example.computershop.exception.AuthenticationException;
import com.example.computershop.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    UserRepository userRepository;
    EmailService emailService;

    public boolean isUserActive(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        return userOpt.map(user -> user.getIsActive() != null && user.getIsActive()).orElse(false);
    }

    public void createUser(@NotNull UserCreationRequest request) {
        try {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new AuthenticationException("Username already exists!");
            }
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new AuthenticationException("Email already exists!");
            }
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new AuthenticationException("Phone number already exists!");
            }
            if (!request.getPassword().equals(request.getPasswordConfirm())) {
                throw new AuthenticationException("Password confirmation does not match!");
            }

            PasswordEncoder encoder = new BCryptPasswordEncoder();
            String encodedPassword = encoder.encode(request.getPassword());

            User user = User.builder()
                    .username(request.getUsername())
                    .password(encodedPassword)
                    .fullName(request.getFullName())
                    .email(request.getEmail())
                    .phoneNumber(request.getPhoneNumber())
                    .role(Role.Customer)
                    .isActive(false)
                    .createdAt(LocalDateTime.now())
                    .address(request.getAddress())
                    .provider("local")
                    .isAccountLocked(false)
                    .build();
            user.setVerificationCode(generateVerificationCode());
            user.setVerificationExpiration(LocalDateTime.now().plusMinutes(15));
            sendVerificationEmail(user);
            userRepository.save(user);
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new AuthenticationException("Error creating user: " + e.getMessage(), e);
        }
    }

    public void verifyUser(VerifyUserRequest verifyUserRequest) {
        try {
            Optional<User> optuser = userRepository.findByEmail(verifyUserRequest.getEmail());
            if (optuser.isPresent()) {
                User user = optuser.get();
                if (user.getVerificationExpiration().isBefore(LocalDateTime.now())) {
                    throw new AuthenticationException("Verification code has expired.");
                }
                user.setIsActive(true);
                user.setVerificationCode(null);
                user.setVerificationExpiration(null);
                userRepository.save(user);
            } else {
                throw new AuthenticationException("User not found.");
            }
        } catch (Exception e) {
            throw new AuthenticationException("Error verifying user: " + e.getMessage(), e);
        }
    }

    public void resendVerificationEmail(String email) {
        try {
            Optional<User> optuser = userRepository.findByEmail(email);
            if (optuser.isPresent()) {
                User user = optuser.get();
                if (user.getIsActive() != null && user.getIsActive()) {
                    throw new AuthenticationException("Account is already activated.");
                }
                user.setVerificationCode(generateVerificationCode());
                user.setVerificationExpiration(LocalDateTime.now().plusHours(1));
                sendVerificationEmail(user);
                userRepository.save(user);
            } else {
                throw new AuthenticationException("User not found.");
            }
        } catch (Exception e) {
            throw new AuthenticationException("Error resending verification email: " + e.getMessage(), e);
        }
    }

    private void sendVerificationEmail(User user) {
        try {
            String subject = "Verify Account - Computer Shop";
            String verificationCode = user.getVerificationCode();
            String verificationLink = "http://localhost:8080/auth/verify?email=" + user.getEmail() + "&code=" + verificationCode;
            String manualverificationLink = "http://localhost:8080/auth/manual-verify";
            String htmlMessage = "<html>"
                    + "<head><meta charset=\"UTF-8\"></head>"
                    + "<body style=\"font-family: Arial, sans-serif;\">"
                    + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                    + "<h2 style=\"color: #333;\">Welcome to Computer Shop!</h2>"
                    + "<p style=\"font-size: 16px;\">Please click the button below to verify your account:</p>"
                    + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                    + "<a href=\"" + verificationLink + "\" style=\"display: inline-block; background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; font-weight: bold;\">Verify Account</a>"
                    + "<p style=\"margin-top: 20px; font-size: 14px; color: #666;\">If the button does not work, you can:</p>"
                    + "<p style=\"font-size: 14px; color: #666;\">1. Access this link: <a href=\"" + manualverificationLink + "\" style=\"color: #007bff;\">" + manualverificationLink + "</a></p>"
                    + "<p style=\"font-size: 14px; color: #666;\">2. Enter verification information:</p>"
                    + "<ul style=\"font-size: 14px; color: #666;\">"
                    + "<li>Email: <span style=\"font-weight: bold;\">" + user.getEmail() + "</span></li>"
                    + "<li>Verification code: <span style=\"font-weight: bold;\">" + verificationCode + "</span></li>"
                    + "</ul>"
                    + "</div>"
                    + "</div>"
                    + "</body>"
                    + "</html>";
            emailService.sendEmail(user.getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            throw new AuthenticationException("Error sending verification email: " + e.getMessage(), e);
        }
    }

    private String generateVerificationCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public void sendPasswordResetEmail(String username) {
        try {
            Optional<User> userOpt = userRepository.findByUsernameOrEmail(username, username);
            if (userOpt.isEmpty()) {
                throw new AuthenticationException("Account not found with this information.");
            }

            User user = userOpt.get();
            String resetToken = generateVerificationCode();
            user.setVerificationCode(resetToken);
            user.setVerificationExpiration(LocalDateTime.now().plusMinutes(15));
            userRepository.save(user);

            String subject = "Reset Password - Computer Shop";
            String resetLink = "http://localhost:8080/auth/reset-password?email=" + user.getEmail() + "&token=" + resetToken;
            String htmlMessage = "<html>"
                    + "<head><meta charset=\"UTF-8\"></head>"
                    + "<body style=\"font-family: Arial, sans-serif;\">"
                    + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                    + "<h2 style=\"color: #333;\">Reset Password Request</h2>"
                    + "<p style=\"font-size: 16px;\">You have requested to reset your password. Please click the button below to continue:</p>"
                    + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                    + "<a href=\"" + resetLink + "\" style=\"display: inline-block; background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; font-weight: bold;\">Reset Password</a>"
                    + "<p style=\"margin-top: 20px; font-size: 14px; color: #666;\">This link will expire in 15 minutes.</p>"
                    + "<p style=\"font-size: 14px; color: #666;\">If you did not request a password reset, please ignore this email.</p>"
                    + "</div>"
                    + "</div>"
                    + "</body>"
                    + "</html>";

            emailService.sendEmail(user.getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            throw new AuthenticationException("Error sending reset password email: " + e.getMessage(), e);
        }
    }

    public boolean validatePasswordResetToken(String email, String token) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        return user.getVerificationCode() != null &&
               user.getVerificationCode().equals(token) &&
               user.getVerificationExpiration() != null &&
               user.getVerificationExpiration().isAfter(LocalDateTime.now());
    }

    public void resetPassword(String email, String token, String newPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new AuthenticationException("Account not found.");
        }

        User user = userOpt.get();
        if (!validatePasswordResetToken(email, token)) {
            throw new AuthenticationException("Invalid or expired token.");
        }

        PasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(newPassword));
        user.setVerificationCode(null);
        user.setVerificationExpiration(null);
        userRepository.save(user);
    }
}
