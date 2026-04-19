package com.project.ratemaster.service;

import com.project.ratemaster.config.TierConfig;
import com.project.ratemaster.dto.RuleRequest;
import com.project.ratemaster.dto.RuleResponse;
import com.project.ratemaster.model.RateLimitRule;
import com.project.ratemaster.model.Tier;
import com.project.ratemaster.repository.RateLimitRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleService {
    private final RateLimitRuleRepository ruleRepository;
    private final TierConfig tierConfig; // fallback to default

    /* Get limit for endpoint+tier
    * Falls back to TierConfig default if no rule found */
    public int getLimit(String endpoint, Tier tier){
        return ruleRepository
                .findByEndpointAndTierAndActiveTrue(endpoint, tier)
                .map(RateLimitRule::getRequestsPerMinute)
                .orElse(tierConfig.getrequestsPerMinute(tier));
    }

    public RuleResponse createRule(RuleRequest request){
        /* Create if rule already exists for this endpoint+ tier */
        ruleRepository.findByEndpointAndTierAndActiveTrue(
                request.getEndpoint(),request.getTier())
                .ifPresent(r->{
                    throw new RuntimeException("Ruke already exists for "+request.getEndpoint()+" + "
                    +request.getTier());
                });

        RateLimitRule rule=RateLimitRule.builder()
                .endpoint(request.getEndpoint())
                .tier(request.getTier())
                .requestsPerMinute(request.getRequestPerMinute())
                .description(request.getDescription())
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        RateLimitRule saved=ruleRepository.save(rule);
        log.info("Rule created: {} {} → {}/min",
                saved.getEndpoint(), saved.getTier(),
                saved.getRequestsPerMinute());

        return toResponse(saved);
    }

    public List<RuleResponse> getAllRules(){
        return ruleRepository.findByActiveTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public RuleResponse updateRule(String id, RuleRequest request){
        RateLimitRule rule=ruleRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Rule not found"));

        rule.setRequestsPerMinute(request.getRequestPerMinute());
        rule.setDescription(request.getDescription());
        RateLimitRule updated= ruleRepository.save(rule);

        log.info("Rule updated: {} → {}/min",
                updated.getEndpoint(),
                updated.getRequestsPerMinute());
        return toResponse(updated);

    }

    public void deleteRule(String id){
        RateLimitRule rule=ruleRepository.findById(id)
                .orElseThrow(()->new RuntimeException("Rule not found"));
        rule.setActive(false);
        ruleRepository.save(rule);
        log.info("Rule deactivated : "+id);
    }

    private RuleResponse toResponse(RateLimitRule saved) {
        return RuleResponse.builder()
                .id(saved.getId())
                .endpoint(saved.getEndpoint())
                .tier(saved.getTier())
                .requestPerMinute(saved.getRequestsPerMinute())
                .active(saved.isActive())
                .description(saved.getDescription())
                .build();
    }

}
