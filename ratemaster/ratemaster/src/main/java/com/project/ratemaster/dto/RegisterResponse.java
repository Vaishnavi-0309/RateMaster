package com.project.ratemaster.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {
    private String apiKey;
    private String tier;
    private String message;
}
