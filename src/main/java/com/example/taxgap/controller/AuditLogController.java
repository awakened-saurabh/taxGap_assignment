package com.example.taxgap.controller;

import com.example.taxgap.domain.AuditLog;
import com.example.taxgap.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditService auditService;

    @GetMapping
    public List<AuditLog> getLogs(@RequestParam(required = false) String transactionId) {
        return auditService.findByTransactionId(transactionId);
    }
}
