package com.project.ratemaster.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RateLimitResult {
    private boolean allowed;
    private int remainingRequests;
    private long resetInSeconds;
    private String algorithm;
    private String message;
}
