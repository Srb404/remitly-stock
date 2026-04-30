package com.remitly.exchange.controller;

import com.remitly.exchange.dto.AuditLogEntryDto;
import com.remitly.exchange.dto.AuditLogResponse;
import com.remitly.exchange.service.AuditLogService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/log")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public AuditLogResponse list() {
        List<AuditLogEntryDto> entries = auditLogService.listAll().stream()
                .map(AuditLogEntryDto::from)
                .toList();
        return new AuditLogResponse(entries);
    }
}