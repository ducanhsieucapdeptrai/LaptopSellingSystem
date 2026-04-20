package com.example.computershop.service;

import com.example.computershop.dto.request.UserCreateByAdmin;
import com.example.computershop.dto.request.UserUpdateByAdmin;
import com.example.computershop.dto.UserProfileData;
import com.example.computershop.dto.request.UserInfoUpdateRequest;
import com.example.computershop.dto.request.PasswordChangeRequest;
import com.example.computershop.entity.User;
import com.example.computershop.repository.UserRepository;
import com.example.computershop.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    /**
     * Lấy thông tin user hiện tại từ Security Context
     * Xử lý cả OAuth2 và Form Authentication
     * 
     * @return UserProfileData chứa user và metadata
     */
    public UserProfileData getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() ||
                    authentication.getName().equals("anonymousUser")) {
                throw new IllegalStateException("Người dùng chưa đăng nhập");
            }

            if (authentication instanceof OAuth2AuthenticationToken) {
                return handleOAuth2Authentication((OAuth2AuthenticationToken) authentication);
            } else {
                return handleFormAuthentication(authentication);
            }

        } catch (Exception e) {
            log.error("Error getting current user", e);
            throw new IllegalStateException("Không thể lấy thông tin người dùng: " + e.getMessage(), e);
        }
    }

    private UserProfileData handleOAuth2Authentication(OAuth2AuthenticationToken oauth2Token) {
        OAuth2User oauth2User = oauth2Token.getPrincipal();
        String provider = oauth2Token.getAuthorizedClientRegistrationId();

        String email;
        String realUsername = null;

        if ("github".equals(provider)) {
            String directEmail = (String) oauth2User.getAttributes().get("email");
            String githubUsername = (String) oauth2User.getAttributes().get("login");

            if (directEmail != null && !directEmail.isEmpty()) {
                email = directEmail;
                realUsername = githubUsername;
            } else {
                email = githubUsername + "@github.com";
                realUsername = githubUsername;
            }
        } else {
            email = (String) oauth2User.getAttributes().get("email");
            if (email == null) {
                throw new IllegalStateException("Không thể lấy email từ " + provider + " OAuth2");
            }
        }

        // Tìm user trong database bằng email (vì OAuth2 user có username = email)
        User user = userRepository.findByUsername(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng OAuth2: " + email));

        return UserProfileData.builder()
                .user(user)
                .isOAuth2(true)
                .authProvider(provider)
                .oauth2Attributes(oauth2User.getAttributes())
                .oauth2Authorities(oauth2User.getAuthorities())
                .realUsername(realUsername)
                .build();
    }

    private UserProfileData handleFormAuthentication(Authentication authentication) {
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng: " + username));

        return UserProfileData.builder()
                .user(user)
                .isOAuth2(false)
                .authProvider("local")
                .build();
    }

    @Transactional
    public void createUserByAdmin(@NotNull UserCreateByAdmin request) {
        try {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Tên đăng nhập đã được sử dụng!");
            }
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("Email đã được đăng ký!");
            }
            if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty() &&
                    userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                throw new IllegalArgumentException("Số điện thoại đã được đăng ký!");
            }

            PasswordEncoder encoder = new BCryptPasswordEncoder();
            String encodedPassword = encoder.encode(request.getPassword());

            User user = User.builder()
                    .username(request.getUsername())
                    .password(encodedPassword)
                    .fullName(request.getFullName())
                    .email(request.getEmail())
                    .phoneNumber(request.getPhoneNumber())
                    .role(Role.valueOf(request.getRole().toUpperCase()))
                    .isActive(request.getActive())
                    .createdAt(LocalDateTime.now())
                    .address(request.getAddress())
                    .provider("local")
                    .isAccountLocked(false)
                    .build();

            userRepository.save(user);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Lỗi khi tạo người dùng: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Boolean toggleStatus(String userId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                user.setIsAccountLocked(!user.getIsAccountLocked());
                userRepository.save(user);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional
    public void updateByAdmin(@NotNull UserUpdateByAdmin userUpdateByAdmin) {
        try {
            User user = userRepository.findById(userUpdateByAdmin.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

            if (!user.getUsername().equals(userUpdateByAdmin.getUsername()) &&
                    userRepository.existsByUsername(userUpdateByAdmin.getUsername())) {
                throw new IllegalArgumentException("Tên đăng nhập đã tồn tại trong hệ thống");
            }
            if (!user.getEmail().equals(userUpdateByAdmin.getEmail()) &&
                    userRepository.existsByEmail(userUpdateByAdmin.getEmail())) {
                throw new IllegalArgumentException("Email đã được sử dụng bởi tài khoản khác");
            }
            if (userUpdateByAdmin.getPhoneNumber() != null && !userUpdateByAdmin.getPhoneNumber().isEmpty() &&
                    !userUpdateByAdmin.getPhoneNumber().equals(user.getPhoneNumber()) &&
                    userRepository.existsByPhoneNumber(userUpdateByAdmin.getPhoneNumber())) {
                throw new IllegalArgumentException("Số điện thoại đã được sử dụng bởi tài khoản khác");
            }

            user.setUsername(userUpdateByAdmin.getUsername());
            user.setFullName(userUpdateByAdmin.getFullName());
            user.setEmail(userUpdateByAdmin.getEmail());
            user.setPhoneNumber(userUpdateByAdmin.getPhoneNumber());
            user.setAddress(userUpdateByAdmin.getAddress());
            user.setRole(userUpdateByAdmin.getRole());
            user.setIsActive(userUpdateByAdmin.getActive());
            user.setIsAccountLocked(userUpdateByAdmin.getIsAccountLocked());

            if (userUpdateByAdmin.getPassword() != null && !userUpdateByAdmin.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode(userUpdateByAdmin.getPassword()));
            }

            userRepository.save(user);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Lỗi khi tạo người dùng: " + e.getMessage(), e);
        }
    }

    /**
     * Cập nhật thông tin cá nhân của user hiện tại (không bao gồm mật khẩu)
     * 
     * @param request UserInfoUpdateRequest chứa thông tin cần cập nhật
     * @return String message kết quả
     */
    @Transactional
    public String updateUserInfo(@NotNull UserInfoUpdateRequest request) {
        try {
            UserProfileData currentUserData = getCurrentUser();
            User currentUser = currentUserData.getUser();

            // Validate dữ liệu đầu vào
            if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
                return "Họ và tên không được để trống";
            }

            // Kiểm tra số điện thoại trùng
            if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()) {
                if (!request.getPhoneNumber().equals(currentUser.getPhoneNumber()) &&
                        userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                    return "Số điện thoại đã được sử dụng bởi tài khoản khác";
                }
            }

            // Cập nhật thông tin cơ bản
            currentUser.setFullName(request.getFullName().trim());
            currentUser.setPhoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null);
            currentUser.setAddress(request.getAddress() != null ? request.getAddress().trim() : null);

            // Lưu thay đổi
            userRepository.save(currentUser);

            log.info("User info updated successfully for user: {}", currentUser.getUsername());
            return "SUCCESS";

        } catch (Exception e) {
            log.error("Unexpected error during user info update", e);
            return "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại.";
        }
    }

    /**
     * Đổi mật khẩu cho user hiện tại (chỉ cho non-OAuth2 users)
     * 
     * @param request PasswordChangeRequest chứa thông tin mật khẩu
     * @return String message kết quả
     */
    @Transactional
    public String changePassword(@NotNull PasswordChangeRequest request) {
        try {
            // Lấy thông tin user hiện tại
            UserProfileData currentUserData = getCurrentUser();
            User currentUser = currentUserData.getUser();

            // Kiểm tra user có phải OAuth2 không
            if (currentUserData.isOAuth2()) {
                return "Không thể đổi mật khẩu cho tài khoản OAuth2";
            }

            // Kiểm tra mật khẩu hiện tại
            if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
                return "Mật khẩu hiện tại không đúng";
            }

            // Kiểm tra xác nhận mật khẩu
            if (!request.isPasswordConfirmed()) {
                return "Mật khẩu xác nhận không khớp";
            }

            // Mã hóa và cập nhật mật khẩu mới
            String encodedNewPassword = passwordEncoder.encode(request.getNewPassword());
            currentUser.setPassword(encodedNewPassword);

            // Lưu thay đổi
            userRepository.save(currentUser);

            log.info("Password changed successfully for user: {}", currentUser.getUsername());
            return "SUCCESS";

        } catch (IllegalStateException e) {
            log.error("Authentication error during password change: {}", e.getMessage());
            return "Lỗi xác thực: " + e.getMessage();
        } catch (Exception e) {
            log.error("Unexpected error during password change", e);
            return "Đã xảy ra lỗi không mong muốn. Vui lòng thử lại.";
        }
    }

    public long countUsers() {
        return userRepository.countUsers();
    }

    public List<Object[]> getTop5UserByRevenueOrder() {
        Pageable pageable = PageRequest.of(0, 5);
        return userRepository.getUserByRevenueOrder(pageable);
    }

    public List<Object[]> getMonthlyNewUsers(int year) {
        return userRepository.countNewUsersByMonth(year);
    }

    /**
     * Cập nhật avatar cho user
     */
    @Transactional
    public String updateUserAvatar(String userId, String avatarFileName) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                return "Không tìm thấy người dùng";
            }

            user.setImage(avatarFileName);
            userRepository.save(user);

            log.info("Avatar updated for user: {}", user.getUsername());
            return "SUCCESS";

        } catch (Exception e) {
            log.error("Error updating user avatar", e);
            return "Lỗi khi cập nhật ảnh đại diện";
        }
    }

    public String getDisplayAvatarUrl(User user) {
        // Priority: Custom upload > OAuth2 > Default
        if (user.getImage() != null && !user.getImage().isEmpty()) {
            return "/uploads/avatars/" + user.getImage();
        } else {
            return "/assets/images/product/author1.png";
        }
    }

    public String getAvatarSource(User user) {
        if (user.getImage() != null && !user.getImage().isEmpty()) {
            return "custom";
        } else {
            return "default";
        }
    }

    // Helper methods
    public User getUserFromPrincipal(Principal principal) {
        if (principal == null) {
            return null;
        }

        // Handle OAuth2 authentication
        if (principal instanceof OAuth2AuthenticationToken oauth2Token) {
            OAuth2User oauth2User = oauth2Token.getPrincipal();
            String provider = oauth2Token.getAuthorizedClientRegistrationId();
            String email = getEmailFromOAuth2Attributes(provider, oauth2User.getAttributes());

            if (email != null) {
                return userRepository.findByUsername(email).orElse(null);
            }
            return null;
        }

        // Handle form authentication
        String identifier = principal.getName();
        return userRepository.findByUsernameOrEmail(identifier, identifier).orElse(null);
    }

    public String getEmailFromOAuth2Attributes(String provider, Map<String, Object> attributes) {
        try {
            if ("google".equals(provider)) {
                return (String) attributes.get("email");
            } else if ("github".equals(provider)) {
                String email = (String) attributes.get("email");
                if (email == null) {
                    String username = (String) attributes.get("login");
                    if (username != null) {
                        email = username + "@github.com";
                    }
                }
                return email;
            } else if ("facebook".equals(provider)) {
                String email = (String) attributes.get("email");
                if (email == null) {
                    String facebookId = (String) attributes.get("id");
                    if (facebookId != null) {
                        email = "facebook_" + facebookId + "@facebook.com";
                    }
                }
                return email;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public String saveAvatarFile(MultipartFile file, String userId) {
        try {
            // Save ở ngoài project
            String uploadDir = "uploads/avatars/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            String originalFileName = file.getOriginalFilename();
            String fileExtension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            
            String fileName = "avatar_" + userId + "_" + System.currentTimeMillis() + fileExtension;
            Path filePath = uploadPath.resolve(fileName);
            
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("Avatar saved: {}", fileName);
            return fileName;
            
        } catch (Exception e) {
            log.error("Error saving avatar file", e);
            return null;
        }
    }

    public void deleteOldAvatar(String avatarFileName) {
        try {
            if (avatarFileName != null && !avatarFileName.isEmpty()) {
                // Không xóa file OAuth2 avatar vì nó có prefix "oauth2_"
                if (!avatarFileName.startsWith("oauth2_")) {
                    String uploadDir = "uploads/avatars/";
                    Path filePath = Paths.get(uploadDir + avatarFileName);

                    if (Files.exists(filePath)) {
                        Files.delete(filePath);
                        log.info("Old avatar deleted: {}", avatarFileName);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error deleting old avatar: {}", avatarFileName, e);
        }
    }
}
