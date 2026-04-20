package com.example.computershop.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Qualifier("customAuthenticationSuccessHandler")
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        for (GrantedAuthority auth : authentication.getAuthorities()) {
            String authority = auth.getAuthority();

            if ("ROLE_Admin".equals(authority)) {
                response.sendRedirect("/admin/dashboard");
                return;
            }
            if ("ROLE_Customer".equals(authority)) {
                response.sendRedirect("/");
                return;
            }
            if ("ROLE_Sales".equals(authority)) {
                response.sendRedirect("/admin/dashboard");
                return;
            }
            if ("ROLE_Shipper".equals(authority)) {
                response.sendRedirect("/shipper/dashboard");
                return;
            }
        }
        
        response.sendRedirect("/user/shopping-page");
    }
}
