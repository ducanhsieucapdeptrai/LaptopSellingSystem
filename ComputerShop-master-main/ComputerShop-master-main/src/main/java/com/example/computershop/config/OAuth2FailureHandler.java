package com.example.computershop.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@Qualifier("oauth2FailureHandler")
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        String errorMessage = exception.getMessage() != null ? exception.getMessage() : "OAuth2 authentication failed";
        String encodedError = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
        getRedirectStrategy().sendRedirect(request, response, "/auth/login?oauth2error=" + encodedError);
    }
}
