package com.remitly.exchange.service;

import com.remitly.exchange.domain.AuditLogEntry;
import com.remitly.exchange.repository.AuditLogRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public List<AuditLogEntry> listAll() {
        return auditLogRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }
}