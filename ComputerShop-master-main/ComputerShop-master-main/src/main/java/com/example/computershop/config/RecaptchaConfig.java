package com.example.computershop.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "google.recaptcha")
public class RecaptchaConfig {
    private String siteKey;
    private String secretKey;
    private String verifyUrl;
} 