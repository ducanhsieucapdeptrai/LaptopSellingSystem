package com.example.computershop.service;

import com.example.computershop.config.RecaptchaConfig;
import com.example.computershop.dto.RecaptchaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecaptchaService {
    
    private final RecaptchaConfig recaptchaConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    
    public boolean isValidCaptcha(String captchaResponse) {
        if (captchaResponse == null || captchaResponse.isEmpty()) {
            log.warn("reCAPTCHA response is empty");
            return false;
        }
        
        try {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("secret", recaptchaConfig.getSecretKey());
            params.add("response", captchaResponse);
            
            RecaptchaResponse response = restTemplate.postForObject(
                recaptchaConfig.getVerifyUrl(), 
                params, 
                RecaptchaResponse.class
            );
            
            if (response != null) {
                log.debug("reCAPTCHA verification result: {}", response.isSuccess());
                if (!response.isSuccess() && response.getErrorCodes() != null) {
                    log.warn("reCAPTCHA verification failed with errors: {}", response.getErrorCodes());
                }
                return response.isSuccess();
            }
            
            log.error("reCAPTCHA response is null");
            return false;
            
        } catch (Exception e) {
            log.error("Error verifying reCAPTCHA: ", e);
            return false;
        }
    }
} 