package com.example.taxgap.service;

import com.example.taxgap.domain.EventType;
import com.example.taxgap.domain.FinancialTransaction;
import com.example.taxgap.domain.TaxRule;
import com.example.taxgap.repository.TaxRuleRepository;
import com.example.taxgap.rules.ComplianceRule;
import com.example.taxgap.rules.RuleViolation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.taxgap.service.AuditService.details;

@Slf4j
@Service
public class RuleEngineService {

    private final TaxRuleRepository ruleRepository;
    private final Map<String, ComplianceRule> implementations;
    private final ExceptionService exceptionService;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public RuleEngineService(TaxRuleRepository ruleRepository, List<ComplianceRule> rules,
                             ExceptionService exceptionService, AuditService auditService,
                             ObjectMapper objectMapper) {
        this.ruleRepository = ruleRepository;
        this.implementations = rules.stream().collect(Collectors.toMap(ComplianceRule::type, Function.identity()));
        this.exceptionService = exceptionService;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    public int execute(FinancialTransaction txn) {
        int raised = 0;

        for (TaxRule definition : ruleRepository.findByEnabledTrue()) {
            ComplianceRule rule = implementations.get(definition.getRuleType());
            if (rule == null) {
                log.warn("No implementation found for rule type {}", definition.getRuleType());
                continue;
            }

            Optional<RuleViolation> violation;
            try {
                JsonNode config = objectMapper.readTree(definition.getConfigJson());
                violation = rule.evaluate(txn, config);
            } catch (JsonProcessingException e) {
                log.error("Invalid config for rule {}", definition.getRuleName(), e);
                continue;
            }

            auditService.log(EventType.RULE_EXECUTION, txn.getTransactionId(),
                    details("rule", definition.getRuleName(), "ruleType", definition.getRuleType(),
                            "config", definition.getConfigJson(), "triggered", violation.isPresent(),
                            "message", violation.map(RuleViolation::message).orElse(null)));

            if (violation.isPresent()) {
                RuleViolation v = violation.get();
                exceptionService.raise(txn.getTransactionId(), txn.getCustomerId(),
                        definition.getRuleName(), v.severity(), v.message());
                raised++;
            }
        }
        return raised;
    }
}
