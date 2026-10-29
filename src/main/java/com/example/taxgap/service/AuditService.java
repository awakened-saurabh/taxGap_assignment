package com.example.taxgap.service;

import com.example.taxgap.domain.AuditLog;
import com.example.taxgap.domain.EventType;
import com.example.taxgap.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public void log(EventType eventType, String transactionId, Map<String, Object> detail) {
        AuditLog entry = new AuditLog();
        entry.setEventType(eventType);
        entry.setTransactionId(transactionId);
        entry.setTimestamp(LocalDateTime.now());
        entry.setDetailJson(toJson(detail));
        auditLogRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> findByTransactionId(String transactionId) {
        if (transactionId == null || transactionId.isBlank()) {
            return auditLogRepository.findAll();
        }
        return auditLogRepository.findByTransactionIdOrderByIdAsc(transactionId);
    }

    public static Map<String, Object> details(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            map.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        return map;
    }

    private String toJson(Map<String, Object> detail) {
        try {
            return objectMapper.writeValueAsString(detail);
        } catch (JsonProcessingException e) {
            log.error("Could not serialise audit detail", e);
            return "{}";
        }
    }
}
