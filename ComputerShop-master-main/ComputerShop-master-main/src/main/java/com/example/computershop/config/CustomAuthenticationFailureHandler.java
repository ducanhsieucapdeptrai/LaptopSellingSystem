package com.example.computershop.config;

import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String error = "Đăng nhập không thành công";
        if (exception instanceof LockedException) {
            error = "Tài khoản đã bị khóa";
        } else if (exception instanceof DisabledException) {
            error = "Tài khoản chưa được kích hoạt";
        } else if( exception instanceof BadCredentialsException) {
            error = "Tên đăng nhập hoặc mật khẩu không đúng";
        }
        response.sendRedirect("/auth/login?error=" + java.net.URLEncoder.encode(error, StandardCharsets.UTF_8));
    }
}

