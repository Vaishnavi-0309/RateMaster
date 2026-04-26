package com.project.ratemaster.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuditEvent {
    private String clientId;
    private String clientEmail;
    private String endpoint;
    private String tier;
    private String algorithm;
    private boolean allowed;
    private int remainingRequests;
    private String timeStamp;
    private String ipAddress;

}
