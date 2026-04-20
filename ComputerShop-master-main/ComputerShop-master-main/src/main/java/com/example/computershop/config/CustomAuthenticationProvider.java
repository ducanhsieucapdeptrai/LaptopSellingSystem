package com.example.computershop.config;

import com.example.computershop.entity.User;
import com.example.computershop.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    public CustomAuthenticationProvider(UserRepository userRepository, 
                                      PasswordEncoder passwordEncoder,
                                      UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BadCredentialsException("Tên đăng nhập không tồn tại"));

        if (Boolean.TRUE.equals(user.getIsAccountLocked())) {
            throw new LockedException("Tài khoản của bạn đã bị khóa");
        }

        if (!user.getIsActive()) {
            throw new DisabledException("Vui lòng xác thực email của bạn trước khi đăng nhập. Kiểm tra hộp thư của bạn để lấy mã xác thực.");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Mật khẩu không chính xác");
        }

        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
} 