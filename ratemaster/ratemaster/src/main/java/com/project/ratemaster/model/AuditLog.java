package com.project.ratemaster.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String clientId;
    private String clientEmail;
    private String endpoint;
    private String tier;
    private String algorithm;
    private boolean allowed;
    private int remainingRequests;
    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime createdAt;

}
