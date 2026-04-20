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
                throw new AuthenticationException("Tên đăng nhập đã được sử dụng!");
            }
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new AuthenticationException("Email đã được đăng ký!");
            }
            if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new AuthenticationException("Số điện thoại đã được đăng ký!");
            }
            if (!request.getPassword().equals(request.getPasswordConfirm())) {
                throw new AuthenticationException("Mật khẩu xác nhận không khớp!");
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
        } catch (Exception e) {
            throw new AuthenticationException("Lỗi khi tạo người dùng: " + e.getMessage(), e);
        }
    }

    public void verifyUser(VerifyUserRequest verifyUserRequest) {
        try {
            Optional<User> optuser = userRepository.findByEmail(verifyUserRequest.getEmail());
            if (optuser.isPresent()) {
                User user = optuser.get();
                if (user.getVerificationExpiration().isBefore(LocalDateTime.now())) {
                    throw new AuthenticationException("Mã xác thực đã hết hạn.");
                }
                user.setIsActive(true);
                user.setVerificationCode(null);
                user.setVerificationExpiration(null);
                userRepository.save(user);
            } else {
                throw new AuthenticationException("Không tìm thấy người dùng.");
            }
        } catch (Exception e) {
            throw new AuthenticationException("Lỗi khi xác thực người dùng: " + e.getMessage(), e);
        }
    }

    public void resendVerificationEmail(String email) {
        try {
            Optional<User> optuser = userRepository.findByEmail(email);
            if (optuser.isPresent()) {
                User user = optuser.get();
                if (user.getIsActive() != null && user.getIsActive()) {
                    throw new AuthenticationException("Tài khoản đã được kích hoạt.");
                }
                user.setVerificationCode(generateVerificationCode());
                user.setVerificationExpiration(LocalDateTime.now().plusHours(1));
                sendVerificationEmail(user);
                userRepository.save(user);
            } else {
                throw new AuthenticationException("Không tìm thấy người dùng.");
            }
        } catch (Exception e) {
            throw new AuthenticationException("Lỗi khi gửi lại email xác thực: " + e.getMessage(), e);
        }
    }

    private void sendVerificationEmail(User user) {
        try {
            String subject = "Xác thực tài khoản Computer Shop";
            String verificationCode = user.getVerificationCode();
            String verificationLink = "http://localhost:8080/auth/verify?email=" + user.getEmail() + "&code=" + verificationCode;
            String manualverificationLink = "http://localhost:8080/auth/manual-verify";
            String htmlMessage = "<html>"
                    + "<head><meta charset=\"UTF-8\"></head>"
                    + "<body style=\"font-family: Arial, sans-serif;\">"
                    + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                    + "<h2 style=\"color: #333;\">Chào mừng bạn đến với Computer Shop!</h2>"
                    + "<p style=\"font-size: 16px;\">Vui lòng nhấn vào nút bên dưới để xác thực tài khoản của bạn:</p>"
                    + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                    + "<a href=\"" + verificationLink + "\" style=\"display: inline-block; background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; font-weight: bold;\">Xác thực tài khoản</a>"
                    + "<p style=\"margin-top: 20px; font-size: 14px; color: #666;\">Nếu nút không hoạt động, bạn có thể:</p>"
                    + "<p style=\"font-size: 14px; color: #666;\">1. Truy cập link này: <a href=\"" + manualverificationLink + "\" style=\"color: #007bff;\">" + manualverificationLink + "</a></p>"
                    + "<p style=\"font-size: 14px; color: #666;\">2. Nhập thông tin xác thực:</p>"
                    + "<ul style=\"font-size: 14px; color: #666;\">"
                    + "<li>Email: <span style=\"font-weight: bold;\">" + user.getEmail() + "</span></li>"
                    + "<li>Mã xác thực: <span style=\"font-weight: bold;\">" + verificationCode + "</span></li>"
                    + "</ul>"
                    + "</div>"
                    + "</div>"
                    + "</body>"
                    + "</html>";

            emailService.sendEmail(user.getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            throw new AuthenticationException("Lỗi khi gửi email xác thực: " + e.getMessage(), e);
        }
    }

    private String generateVerificationCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    public void sendPasswordResetEmail(String username) {
        try {
            Optional<User> userOpt = userRepository.findByUsernameOrEmail(username, username);
            if (userOpt.isEmpty()) {
                throw new AuthenticationException("Không tìm thấy tài khoản với thông tin này.");
            }

            User user = userOpt.get();
            String resetToken = generateVerificationCode();
            user.setVerificationCode(resetToken);
            user.setVerificationExpiration(LocalDateTime.now().plusMinutes(15));
            userRepository.save(user);

            String subject = "Đặt lại mật khẩu - Computer Shop";
            String resetLink = "http://localhost:8080/auth/reset-password?email=" + user.getEmail() + "&token=" + resetToken;
            String htmlMessage = "<html>"
                    + "<head><meta charset=\"UTF-8\"></head>"
                    + "<body style=\"font-family: Arial, sans-serif;\">"
                    + "<div style=\"background-color: #f5f5f5; padding: 20px;\">"
                    + "<h2 style=\"color: #333;\">Yêu cầu đặt lại mật khẩu</h2>"
                    + "<p style=\"font-size: 16px;\">Bạn đã yêu cầu đặt lại mật khẩu. Vui lòng nhấn vào nút bên dưới để tiếp tục:</p>"
                    + "<div style=\"background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                    + "<a href=\"" + resetLink + "\" style=\"display: inline-block; background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; font-weight: bold;\">Đặt lại mật khẩu</a>"
                    + "<p style=\"margin-top: 20px; font-size: 14px; color: #666;\">Link này sẽ hết hạn sau 15 phút.</p>"
                    + "<p style=\"font-size: 14px; color: #666;\">Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.</p>"
                    + "</div>"
                    + "</div>"
                    + "</body>"
                    + "</html>";

            emailService.sendEmail(user.getEmail(), subject, htmlMessage);
        } catch (MessagingException e) {
            throw new AuthenticationException("Lỗi khi gửi email đặt lại mật khẩu: " + e.getMessage(), e);
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
            throw new AuthenticationException("Không tìm thấy tài khoản.");
        }

        User user = userOpt.get();
        if (!validatePasswordResetToken(email, token)) {
            throw new AuthenticationException("Token không hợp lệ hoặc đã hết hạn.");
        }

        PasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(newPassword));
        user.setVerificationCode(null);
        user.setVerificationExpiration(null);
        userRepository.save(user);
    }
}
