package com.project.ratemaster.config;

import com.project.ratemaster.model.Tier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TierConfig {

    @Value("${app.rate-limit.algorithm}")
    private String algorithm;

    public String getAlgorithm(){
        return algorithm;
    }

    public int getrequestsPerMinute(Tier tier){
        return switch (tier){
            case FREE -> 10;
            case PRO -> 100;
            case ENTERPRISE -> 1000;
        };
    }
}
