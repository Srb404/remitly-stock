package com.remitly.exchange.repository;

import com.remitly.exchange.domain.AuditLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLogEntry, Long> { }
