package com.project.ratemaster.service;

import com.project.ratemaster.config.TierConfig;
import com.project.ratemaster.dto.RateLimitResult;
import com.project.ratemaster.model.Tier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimiterService {

    private final RedisTemplate<String,String> redisTemplate;
    private final TierConfig tierConfig;
    private final RuleService ruleService;

    /* ALGORITHM - Fixed Window */
    public RateLimitResult fixedWindow(String clientId, Tier tier, String endpoint){
        int limit= ruleService.getLimit(endpoint,tier);
        long windowSeconds=60L;

        /* Key format: fw:clientId:endpoint:currentWindow */
        long currentWindow=System.currentTimeMillis()/(windowSeconds*1000);
        String key="fw:"+clientId+":"+endpoint+":"+currentWindow;
        Long count=redisTemplate.opsForValue().increment(key);

        if(count==1){
            /* First req in window - set expiry */
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
        }

        long remaining=Math.max(0,limit-count);
        boolean allowed=count<=limit;
        log.info("FixedWindow — client:{} endpoint:{} count:{}/{} allowed:{}",
                clientId, endpoint, count, limit, allowed);

        return RateLimitResult.builder()
                .allowed(allowed)
                .remainingRequests((int) remaining)
                .resetInSeconds(windowSeconds)
                .algorithm("FIXED_WINDOW")
                .message(allowed ? "Request allowed" : "Rate limit exceeded")
                .build();

    }

    /* ALGORITHM - Sliding Window */
    public RateLimitResult slidingWindow(String clientId,Tier tier,String endpoint){
        int limit= ruleService.getLimit(endpoint,tier);
        long windowMillis=60_000L;
        long now=System.currentTimeMillis();

        String key="sw:"+clientId+":"+endpoint;

      /*  Remove timestamps older than 60 seconds */
         redisTemplate.opsForZSet().removeRangeByScore(key,0,now-windowMillis);

         /* Count requests in current window */
        Long count=redisTemplate.opsForZSet().zCard(key);
        count=count==null?0:count;

        boolean allowed=count<limit;

        if(allowed){
            /* Add current timestamp  */
            redisTemplate.opsForZSet().add(key,String.valueOf(now),now);
            redisTemplate.expire(key,Duration.ofSeconds(70));
        }

        long remaining =Math.max(0,limit-count-(allowed?1:0));
        log.info("SlidingWindow — client:{} endpoint:{} count:{}/{} allowed:{}",
                clientId, endpoint, count, limit, allowed);

        return RateLimitResult.builder()
                .allowed(allowed)
                .remainingRequests((int) remaining)
                .resetInSeconds(60)
                .algorithm("SLIDING_WINDOW")
                .message(allowed ?"Request allowed" : "Rate Limit exceeded")
                .build();
    }

    /* ALGORITHM - 3. Token Bucket  */
    public RateLimitResult tokenBucket(String clientId,Tier tier,String endpoint){
        int capacity=ruleService.getLimit(endpoint,tier);
        /* Tokens per second */
        double refillRate=capacity/60.0;
        long now=System.currentTimeMillis();

        String tokensKey="tb:tokens:"+clientId+":"+endpoint;
        String lastRefillKey="tb:lastrefill:"+clientId+":"+endpoint;

        /* Get current tokens and last refill time */
        String tokensStr=redisTemplate.opsForValue().get(tokensKey);
        String lastRefillStr=redisTemplate.opsForValue().get(lastRefillKey);

        double tokens=tokensStr!=null ? Double.parseDouble(tokensStr):capacity;
        long lastRefill=lastRefillStr!=null ? Long.parseLong(lastRefillStr):now;

        /* Refill tokens based on time elapsed */
        double elapsed=(now-lastRefill)/1000.0;
        tokens=Math.min(capacity,tokens+elapsed*refillRate);

        boolean allowed =tokens >=1.0;

        if(allowed){
            tokens-=1.0;
        }
        /* save updated state in cache */
        redisTemplate.opsForValue().set(tokensKey,String.valueOf(tokens),Duration.ofSeconds(120));
        redisTemplate.opsForValue().set(lastRefillKey,String.valueOf(now),Duration.ofSeconds(120));

        log.info("TokenBucket — client:{} endpoint:{} tokens:{} allowed:{}",
                clientId, endpoint, tokens, allowed);
        return RateLimitResult.builder()
                .allowed(allowed)
                .remainingRequests((int) Math.floor(tokens))
                .resetInSeconds(60)
                .algorithm("TOKEN_BUCKET")
                .message(allowed?"Request allowed":"Rate limit exceeded")
                .build();

    }
}
