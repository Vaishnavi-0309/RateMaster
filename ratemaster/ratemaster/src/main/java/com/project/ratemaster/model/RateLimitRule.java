package com.project.ratemaster.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name="rate_limit_rules",
uniqueConstraints = @UniqueConstraint(columnNames = {"endpoint","tier"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateLimitRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String endpoint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tier tier;

    @Column(nullable = false)
    private int requestsPerMinute;

    @Column(nullable = false)
    private boolean active;

    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
