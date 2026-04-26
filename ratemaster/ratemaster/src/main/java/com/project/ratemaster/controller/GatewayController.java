package com.project.ratemaster.controller;

import com.project.ratemaster.dto.AuditEvent;
import com.project.ratemaster.dto.RateLimitResult;
import com.project.ratemaster.kafka.AuditProducer;
import com.project.ratemaster.model.Tier;
import com.project.ratemaster.service.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/gateway")
@RequiredArgsConstructor
@Slf4j
public class GatewayController {
    private final RateLimiterService rateLimiterService;
    private final WebClient.Builder webClientBuilder;
    private final AuditProducer auditProducer;

    @Value("${app.finstream-url}")
    private String finstreamUrl;

    @Value("${app.rate-limit.algorithm}")
    private String algorithm;

    /* Handles ALL HTTP methods - GET,POST,PUT,DELETE  */
    @RequestMapping("/**")
    public ResponseEntity<?> gateway(HttpServletRequest request,
                                     @RequestBody(required=false) String body){
        String clientId=(String) request.getAttribute("clientId");
        Tier tier=(Tier) request.getAttribute("clientTier");
        String clientEmail=(String) request.getAttribute("clientEmail");

        if(clientId==null || tier==null){
            return ResponseEntity.status(401)
                    .body(Map.of("error","Unauthorized"));
        }

        /* Extract the actual endpoint path
        * /gateway/api/transaction -> api/transaction */

        String fullPath=request.getRequestURI();
        String endPoint=fullPath.replace("/gateway","");

        log.info("Gateway request — client:{} endpoint:{} tier:{}",
                clientEmail, endPoint, tier);

        /* Check rate limit */
        RateLimitResult result=switch (algorithm){
            case "FIXED_WINDOW" -> rateLimiterService.fixedWindow(clientId,tier,endPoint);
            case "TOKEN_BUCKET" -> rateLimiterService.tokenBucket(clientId,tier,endPoint);
            default -> rateLimiterService.slidingWindow(clientId,tier,endPoint);
        };

        // Build and fire audit event async
        AuditEvent auditEvent = AuditEvent.builder()
                .clientId(clientId)
                .clientEmail(clientEmail)
                .endpoint(endPoint)
                .tier(tier.name())
                .algorithm(algorithm)
                .allowed(result.isAllowed())
                .remainingRequests(result.getRemainingRequests())
                .ipAddress(request.getRemoteAddr())
                .timeStamp(LocalDateTime.now().toString())
                .build();

        log.info("Firing audit event for client: {}", clientEmail);
        auditProducer.publishEvent(auditEvent);

        if(!result.isAllowed()){
            log.warn("Rate limit exceeded — client:{} endpoint:{}",
                    clientEmail, endPoint);
            return ResponseEntity.status(429)
                    .header("X-RateLimit-Remaining",
                            String.valueOf(result.getRemainingRequests()))
                    .header("X-Ratelimit-Reset",
                            String.valueOf(result.getResetInSeconds()))
                    .header("Retry-After",
                            String.valueOf(result.getResetInSeconds()))
                    .body(Map.of("error","Rate limit exceeded",
                            "remainingRequests",result.getRemainingRequests(),
                            "retryAfterSeconds",result.getResetInSeconds()));
        }
        /* If allowed -> forward to finstream */
        String targetUrl=finstreamUrl+endPoint;
        String method=request.getMethod();
        String queryString= request.getQueryString();

        if(queryString!=null){
            targetUrl=targetUrl+"?"+queryString;
        }
        log.info("Forwarding to FinStream: {} {}", method, targetUrl);

        try{
            WebClient client=webClientBuilder.build();
            /* Build and execute request to Finstream */
            String authHeader = request.getHeader("Authorization");
            WebClient.RequestBodySpec requestBodySpec=client
                    .method(HttpMethod.valueOf(method))
                    .uri(targetUrl)
                    .header("X-Forwarded-By","RateMaster")
                    .header("X-Client-Tier",tier.name());

            // Forward JWT if present
            if (authHeader != null && !authHeader.isBlank()) {
                requestBodySpec.header("Authorization", authHeader);
            }

            /* Add body if present */
            ResponseEntity<String> finstreamResponse;
            if(body!=null && !body.isBlank()){
                finstreamResponse=requestBodySpec
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(body)
                        .retrieve()
                        .toEntity(String.class)
                        .block();
            }else{
                finstreamResponse=requestBodySpec
                        .retrieve()
                        .toEntity(String.class)
                        .block();
            }

            /* Return Finstream response + rate limit headers */
            return ResponseEntity
                    .status(finstreamResponse.getStatusCode())
                    .header("X-RateLimit-Remaining",
                            String.valueOf(result.getRemainingRequests()))
                    .header("X-RateLimit-Reset",String.valueOf(result.getResetInSeconds()))
                    .header("Content-Type","application/json")
                    .body(finstreamResponse.getBody());

        }catch(Exception e){
            log.error("Error forwarding to FinStream: {}", e.getMessage());
            return ResponseEntity.status(502)
                    .body(Map.of("error","FinStream service unavailable: "+e.getMessage()));
        }

    }
}
