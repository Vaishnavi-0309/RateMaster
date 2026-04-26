package com.project.ratemaster.kafka;

import com.project.ratemaster.dto.AuditEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditProducer {

    private final KafkaTemplate<String,String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Async
    public void publishEvent(AuditEvent event){
        try{
            String topic=event.isAllowed() ? "requests.allowed" : "requests.blocked";
            String message=objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic,event.getClientId(),message);
            log.info("Audit event published to topic:{} client:{}",
                    topic, event.getClientId());
        }catch (Exception e){
            log.error("Failed to publish audit event : {}", e.getMessage());
        }
    }

}
