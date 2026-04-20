package com.example.computershop.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RecaptchaResponse {
    private boolean success;
    
    @JsonProperty("challenge_ts")
    private LocalDateTime challengeTs;
    
    private String hostname;
    
    @JsonProperty("error-codes")
    private List<String> errorCodes;
} 