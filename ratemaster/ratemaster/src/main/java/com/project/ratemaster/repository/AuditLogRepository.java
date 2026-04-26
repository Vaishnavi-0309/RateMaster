package com.project.ratemaster.repository;

import com.project.ratemaster.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog,String> {

    List<AuditLog> findByClientId(String clientId);
    long countByAllowedTrueAndCreatedAtAfter(LocalDateTime after);
    long countByAllowedFalseAndCreatedAtAfter(LocalDateTime after);
    long countByCreatedAtAfter(LocalDateTime after);

}
