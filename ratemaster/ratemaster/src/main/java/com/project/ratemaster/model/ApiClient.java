package com.project.ratemaster.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_clients")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiClient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true,nullable = false)
    private String email;

    @Column(nullable = false)
    private String hashedPassword;

    @Column(unique = true)
    private String apiKeyHash;

    @Column(nullable = false)
    private String apiKeyPrefix;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Tier tier;

    @Column(nullable = false)
    private boolean active;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
