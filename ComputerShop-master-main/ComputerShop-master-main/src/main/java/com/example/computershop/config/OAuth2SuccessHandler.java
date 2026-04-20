package com.example.computershop.config;

import com.example.computershop.enums.Role;
import com.example.computershop.entity.User;
import com.example.computershop.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.net.URI;

@Component
@Qualifier("oauth2SuccessHandler")
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UserRepository userRepository;

    public OAuth2SuccessHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        try {
            if (authentication instanceof OAuth2AuthenticationToken oauthToken) {
                String provider = oauthToken.getAuthorizedClientRegistrationId();
                OAuth2User oauth2User = oauthToken.getPrincipal();
                Map<String, Object> attributes = oauth2User.getAttributes();

                String email = getEmailFromAttributes(provider, attributes);
                if (email == null) {
                    redirectWithError(request, response, "email_not_found");
                    return;
                }

                String name = getNameFromAttributes(provider, attributes);
                String avatarUrl = getAvatarUrlFromOAuth2(provider, attributes);
                Optional<User> existingUser = userRepository.findByEmail(email);

                User user;
                if (existingUser.isPresent()) {
                    user = existingUser.get();

                    // ⚠️ Nếu đã tồn tại mà provider là local, từ chối
                    if (user.getProvider() == null || user.getProvider().equalsIgnoreCase("local")) {
                        redirectWithError(request, response, "provider_conflict");
                        return;
                    }

                    // ⚠️ Nếu provider không khớp, từ chối
                    if (!user.getProvider().equalsIgnoreCase(provider)) {
                        redirectWithError(request, response, "provider_mismatch");
                        return;
                    }

                    // ✅ CẬP NHẬT AVATAR CHO USER CŨ (NẾU CHƯA CÓ)
                    if ((user.getImage() == null || user.getImage().isEmpty()) && avatarUrl != null) {
                        String savedAvatarFileName = downloadAndSaveOAuth2Avatar(avatarUrl, user.getUserId(), provider);
                        if (savedAvatarFileName != null) {
                            user.setImage(savedAvatarFileName);
                            userRepository.save(user);
                        }
                    }
                } else {
                    // Tạo user mới trước để có userId
                    String username = generateUniqueUsername(email, provider);
                    String uniquePhoneNumber = generateUniquePhoneNumber(provider);
                    
                    user = User.builder()
                            .email(email)
                            .username(username)
                            .password("")
                            .role(Role.Customer)
                            .isActive(true)
                            .fullName(name != null ? name : email)
                            .createdAt(LocalDateTime.now())
                            .provider(provider)
                            .phoneNumber(uniquePhoneNumber)
                            .address("")
                            .isAccountLocked(false)
                            .build();
                    
                    // Save user first to get ID
                    user = userRepository.save(user);
                    
                    // Now download avatar with real userId
                    if (avatarUrl != null) {
                        String savedAvatarFileName = downloadAndSaveOAuth2Avatar(avatarUrl, user.getUserId(), provider);
                        if (savedAvatarFileName != null) {
                            user.setImage(savedAvatarFileName);
                            userRepository.save(user);
                        }
                    }
                }

                // ✅ GÁN QUYỀN CHO NGƯỜI DÙNG DỰA TRÊN ROLE
                List<GrantedAuthority> authorities = new ArrayList<>();
                String role = user.getRole().name();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
                
                // ✅ Debug để chắc chắn
                authorities.forEach(auth -> System.out.println(">> GrantedAuthority: " + auth.getAuthority()));

                // ✅ Determine nameAttributeKey based on provider and available attributes
                String nameAttributeKey = determineNameAttributeKey(provider, oauth2User.getAttributes());

                // ✅ Tạo user mới có quyền cập nhật
                OAuth2User newOauth2User = new DefaultOAuth2User(
                        authorities,
                        oauth2User.getAttributes(),
                        nameAttributeKey // Sử dụng key phù hợp thay vì hardcode "email"
                );

                OAuth2AuthenticationToken newAuth = new OAuth2AuthenticationToken(
                        newOauth2User,
                        authorities,
                        oauthToken.getAuthorizedClientRegistrationId()
                );

                // ✅ Gán lại vào SecurityContext
                SecurityContextHolder.getContext().setAuthentication(newAuth);
                
                // ✅ Redirect based on user role
                String redirectUrl = determineRedirectUrl(user);
                getRedirectStrategy().sendRedirect(request, response, redirectUrl);
            } else {
                // Fallback for non-OAuth2 authentication
                getRedirectStrategy().sendRedirect(request, response, "/user/shopping-page");
            }

        } catch (Exception e) {
            System.err.println("OAuth2 Authentication Error: " + e.getMessage());
            redirectWithError(request, response, "oauth2_error");
        }
    }

    private void redirectWithError(HttpServletRequest request, HttpServletResponse response, String errorMessage) throws IOException {
        String encodedError = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        getRedirectStrategy().sendRedirect(request, response, "/auth/login?oauth2error=" + encodedError);
    }

    private String getEmailFromAttributes(String provider, Map<String, Object> attributes) {
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

    private String getNameFromAttributes(String provider, Map<String, Object> attributes) {
        try {
            if ("google".equals(provider)) {
                return (String) attributes.get("name");
            } else if ("github".equals(provider)) {
                String name = (String) attributes.get("name");
                if (name == null) {
                    name = (String) attributes.get("login");
                }
                return name;
            } else if ("facebook".equals(provider)) {
                return (String) attributes.get("name");
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String getAvatarUrlFromOAuth2(String provider, Map<String, Object> attributes) {
        try {
            if ("google".equals(provider)) {
                return (String) attributes.get("picture");
            } else if ("github".equals(provider)) {
                return (String) attributes.get("avatar_url");
            } else if ("facebook".equals(provider)) {
                String facebookId = (String) attributes.get("id");
                if (facebookId != null) {
                    return "https://graph.facebook.com/" + facebookId + "/picture?type=large";
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private String generateUniqueUsername(String email, String provider) {
        if (email != null && !email.isEmpty()) {
            if (!userRepository.existsByUsername(email)) {
                return email;
            }
        }

        String baseUsername = provider + "_user";
        String username = baseUsername;
        int counter = 1;

        while (userRepository.existsByUsername(username)) {
            username = baseUsername + "_" + counter;
            counter++;
        }

        return username;
    }

    private String generateUniquePhoneNumber(String provider) {
        long timestamp = System.currentTimeMillis();
        String basePhoneNumber = provider + "_" + timestamp;
        String phoneNumber = basePhoneNumber;
        int counter = 1;

        while (userRepository.existsByPhoneNumber(phoneNumber)) {
            phoneNumber = basePhoneNumber + "_" + counter;
            counter++;
        }

        return phoneNumber;
    }
    
    private String determineRedirectUrl(User user) {
        Role userRole = user.getRole();

        return switch (userRole) {
            case Admin,Sales -> "/admin/dashboard";
            case Customer -> "/";
            case Shipper -> "/shipper/dashboard";
            default -> "/user/shopping-page";
        };
    }

    private String downloadAndSaveOAuth2Avatar(String avatarUrl, String userId, String provider) {
        try {
            if (avatarUrl == null || avatarUrl.isEmpty()) {
                return null;
            }
            
            System.out.println("Downloading OAuth2 avatar from: " + avatarUrl);
            
            // Tạo thư mục nếu chưa có
            String uploadDir = "src/main/resources/static/uploads/avatars/";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Download ảnh từ URL
            URI uri = new URI(avatarUrl);
            URL url = uri.toURL();
            try (InputStream in = url.openStream()) {
                // Tạo tên file unique với userId thật
                String fileName = "oauth2_" + provider + "_" + userId + "_" + System.currentTimeMillis() + ".jpg";
                Path filePath = uploadPath.resolve(fileName);
                
                // Lưu file
                Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
                
                System.out.println("OAuth2 avatar saved: " + fileName);
                return fileName;
            }
            
        } catch (Exception e) {
            System.err.println("Error downloading OAuth2 avatar: " + e.getMessage());
            return null;
        }
    }

    private String determineNameAttributeKey(String provider, Map<String, Object> attributes) {
        try {
            // Kiểm tra provider và attributes có sẵn
            switch (provider.toLowerCase()) {
                case "google":
                    // Google luôn có "sub" và thường có "email"
                    if (attributes.containsKey("email") && attributes.get("email") != null) {
                        return "email";
                    }
                    return "sub"; // Google ID

                case "github":
                    // GitHub có thể không có email public
                    if (attributes.containsKey("email") && attributes.get("email") != null) {
                        return "email";
                    }
                    return "login"; // GitHub username

                case "facebook":
                    // Facebook thường có "id" và có thể có "email"
                    if (attributes.containsKey("email") && attributes.get("email") != null) {
                        return "email";
                    }
                    return "id"; // Facebook ID

                default:
                    // Fallback: tìm attribute không null
                    for (String key : new String[]{"email", "sub", "id", "login", "name"}) {
                        if (attributes.containsKey(key) && attributes.get(key) != null) {
                            return key;
                        }
                    }
                    // Last resort
                    return attributes.keySet().iterator().next();
            }
        } catch (Exception e) {
            System.err.println("Error determining name attribute key: " + e.getMessage());
            // Return first available key as fallback
            return attributes.keySet().iterator().next();
        }
    }
}
