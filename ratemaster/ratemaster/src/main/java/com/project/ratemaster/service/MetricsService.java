package com.project.ratemaster.service;

import com.project.ratemaster.dto.MetricsResponse;
import com.project.ratemaster.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsService {
    private final AuditLogRepository auditLogRepository;

    /* Total summary for today */
    public MetricsResponse getSummary(){
        LocalDateTime startOfDay=LocalDateTime.now()
                .withHour(0).withMinute(0).withSecond(0);

        long total= auditLogRepository.countByCreatedAtAfter(startOfDay);
        long allowed = auditLogRepository.countByAllowedTrueAndCreatedAtAfter(startOfDay);
        long blocked = auditLogRepository.countByAllowedFalseAndCreatedAtAfter(startOfDay);

        double blockRate = total > 0 ?
                Math.round((blocked * 100.0 / total) * 100.0) / 100.0 : 0.0;

        return MetricsResponse.builder()
                .totalRequests(total)
                .allowedRequests(allowed)
                .blockedRequests(blocked)
                .blockedRatePercentage(blockRate)
                .period("today")
                .build();

    }

    /* Statistics for specific client */
    public Map<String,Object> getClientStats(String clientId){
        LocalDateTime startOfDay=LocalDateTime.now()
                .withHour(0).withMinute(0).withSecond(0);

        var logs = auditLogRepository.findByClientId(clientId);
        long total=logs.size();
        long allowed = logs.stream()
                .filter(l->l.isAllowed()).count();
        long blocked=logs.stream()
                .filter(l->!l.isAllowed()).count();
        double blockRate=total>0 ? Math.round((blocked*100.0 / total) * 100.0) / 100.0 : 0.0;
        return Map.of(
           "clientId",clientId,
           "totalRequests",total,
                "allowedRequests",blocked,
                "blockRatePercentage",blockRate
        );
    }

    /* Top 10 consumers */
    public List<Map<String,Object>> getTopConsumers(){
        return auditLogRepository.findAll()
                .stream()
                .collect(
                        Collectors.groupingBy(
                                logs->logs.getClientEmail(),
                                Collectors.counting()
                        ))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String,Long>comparingByValue()
                        .reversed())
                .limit(10)
                .map(entry -> Map.<String,Object>of(
                        "clientEmail",entry.getKey(),
                        "totalRequests",entry.getValue()
                )).collect(Collectors.toList());
    }

    /* All blocked requests today */
    public List<Map<String,Object>> getBlockedRequests(){
        LocalDateTime startOfDay=LocalDateTime.now()
                .withHour(0).withMinute(0).withSecond(0);

        return auditLogRepository.findAll()
                .stream()
                .filter(l->!l.isAllowed() &&
                        l.getCreatedAt().isAfter(startOfDay))
                .map(l-> Map.<String,Object>of(
                        "clientEmail",l.getClientEmail(),
                        "endpoint",l.getEndpoint(),
                        "tier",l.getTier(),
                        "timestamp",l.getCreatedAt().toString()
                )).collect(Collectors.toList());
    }
}
