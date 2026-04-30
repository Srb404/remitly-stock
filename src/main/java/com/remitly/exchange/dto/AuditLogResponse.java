package com.remitly.exchange.dto;

import java.util.List;

public record AuditLogResponse(List<AuditLogEntryDto> log) { }
