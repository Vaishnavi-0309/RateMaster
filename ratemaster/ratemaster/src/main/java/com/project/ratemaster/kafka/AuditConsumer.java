package com.project.ratemaster.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.ratemaster.dto.AuditEvent;
import com.project.ratemaster.model.AuditLog;
import com.project.ratemaster.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditConsumer {
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics={"requests.allowed","requests.blocked"},
    groupId = "ratemaster-audit-group")
    public void consume(String message){
        try{
            AuditEvent event=objectMapper.readValue(message,AuditEvent.class);
            AuditLog auditLog =AuditLog.builder()
                    .clientEmail(event.getClientEmail())
                    .clientId(event.getClientId())
                    .endpoint(event.getEndpoint())
                    .tier(event.getTier())
                    .algorithm(event.getAlgorithm())
                    .allowed(event.isAllowed())
                    .remainingRequests(event.getRemainingRequests())
                    .ipAddress(event.getIpAddress())
                    .createdAt(LocalDateTime.now())
                    .build();
            auditLogRepository.save(auditLog);
        }catch(Exception e){
            log.error("Failed to consume audit event : {}", e.getMessage());

        }
    }
}
