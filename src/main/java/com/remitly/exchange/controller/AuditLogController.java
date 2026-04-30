package com.remitly.exchange.controller;

import com.remitly.exchange.dto.AuditLogEntryDto;
import com.remitly.exchange.dto.AuditLogResponse;
import com.remitly.exchange.service.AuditLogService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/log")
@Validated
public class AuditLogController {

    public static final int DEFAULT_LIMIT = 100;
    public static final int MAX_LIMIT = 1000;

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public AuditLogResponse list(
            @RequestParam(name = "afterId", defaultValue = "0") @Min(0) long afterId,
            @RequestParam(name = "limit", defaultValue = "" + DEFAULT_LIMIT)
            @Min(1) @Max(MAX_LIMIT) int limit) {
        List<AuditLogEntryDto> entries = auditLogService.listPage(afterId, limit).stream()
                .map(AuditLogEntryDto::from)
                .toList();
        return new AuditLogResponse(entries);
    }
}