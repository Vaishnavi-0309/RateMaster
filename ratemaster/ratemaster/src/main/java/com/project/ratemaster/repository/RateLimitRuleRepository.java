package com.project.ratemaster.repository;

import com.project.ratemaster.model.RateLimitRule;
import com.project.ratemaster.model.Tier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RateLimitRuleRepository extends JpaRepository<RateLimitRule,String> {

    Optional<RateLimitRule> findByEndpointAndTierAndActiveTrue(String endpoint , Tier tier);
    List<RateLimitRule> findByActiveTrue();
    List<RateLimitRule> findByEndpoint(String endpoint);
}
