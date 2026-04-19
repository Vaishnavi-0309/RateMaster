package com.project.ratemaster.dto;

import com.project.ratemaster.model.Tier;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RuleResponse {
    private String id;
    private String endpoint;
    private Tier tier;
    private int requestPerMinute;
    private boolean active;
    private String description;

}
