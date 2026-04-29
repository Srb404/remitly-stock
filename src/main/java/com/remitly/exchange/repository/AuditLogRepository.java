package com.remitly.exchange.repository;

import com.remitly.exchange.domain.AuditLogEntry;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLogEntry, Long> {

    List<AuditLogEntry> findByIdGreaterThanOrderByIdAsc(long afterId, Pageable pageable);
}