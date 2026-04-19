package com.project.ratemaster.dto;

import com.project.ratemaster.model.Tier;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RuleRequest {

    @NotBlank(message = "Endpoint is required")
    private String endpoint;

    @NotNull(message = "Tier is required")
    private Tier tier;

    @Min(value=1,message = "Min 1 request per minute")
    @Max(value=10000,message = "Max 10000 request per minute")
    private int requestPerMinute;
    private String description;
}
