package com.project.ratemaster.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {
    @Bean
    public NewTopic allowedTopic(){
        return TopicBuilder.name("requests.allowed")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic blockedTopic(){
        return TopicBuilder.name("requests.blocked")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
