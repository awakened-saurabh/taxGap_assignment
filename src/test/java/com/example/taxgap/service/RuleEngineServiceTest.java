package com.example.taxgap.service;

import com.example.taxgap.TestData;
import com.example.taxgap.domain.EventType;
import com.example.taxgap.domain.FinancialTransaction;
import com.example.taxgap.domain.Severity;
import com.example.taxgap.domain.TaxRule;
import com.example.taxgap.domain.TransactionType;
import com.example.taxgap.repository.TaxRuleRepository;
import com.example.taxgap.rules.ComplianceRule;
import com.example.taxgap.rules.RuleViolation;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RuleEngineServiceTest {

    @Mock
    private TaxRuleRepository ruleRepository;
    @Mock
    private ExceptionService exceptionService;
    @Mock
    private AuditService auditService;

    private RuleEngineService engine;
    private FinancialTransaction txn;

    private final ComplianceRule alwaysFires = new ComplianceRule() {
        @Override
        public String type() {
            return "ALWAYS";
        }

        @Override
        public Optional<RuleViolation> evaluate(FinancialTransaction t, JsonNode config) {
            return Optional.of(new RuleViolation(Severity.LOW, "boom"));
        }
    };

    private final ComplianceRule neverFires = new ComplianceRule() {
        @Override
        public String type() {
            return "NEVER";
        }

        @Override
        public Optional<RuleViolation> evaluate(FinancialTransaction t, JsonNode config) {
            return Optional.empty();
        }
    };

    @BeforeEach
    void setUp() {
        engine = new RuleEngineService(ruleRepository, List.of(alwaysFires, neverFires),
                exceptionService, auditService, new ObjectMapper());
        txn = TestData.txn("T1", "C1", "100", "0.18", TransactionType.SALE);
    }

    @Test
    void raisesExceptionWhenRuleFires() {
        when(ruleRepository.findByEnabledTrue()).thenReturn(List.of(new TaxRule("Fire Rule", "ALWAYS", "{}", true)));

        int raised = engine.execute(txn);

        assertEquals(1, raised);
        verify(exceptionService).raise("T1", "C1", "Fire Rule", Severity.LOW, "boom");
    }

    @Test
    void logsEveryRuleExecutionEvenWhenNothingFires() {
        when(ruleRepository.findByEnabledTrue()).thenReturn(List.of(
                new TaxRule("Fire Rule", "ALWAYS", "{}", true),
                new TaxRule("Quiet Rule", "NEVER", "{}", true)));

        int raised = engine.execute(txn);

        assertEquals(1, raised);
        verify(auditService, org.mockito.Mockito.times(2)).log(eq(EventType.RULE_EXECUTION), eq("T1"), anyMap());
    }

    @Test
    void skipsRulesWithoutImplementation() {
        when(ruleRepository.findByEnabledTrue()).thenReturn(List.of(new TaxRule("Ghost", "UNKNOWN", "{}", true)));

        assertEquals(0, engine.execute(txn));
        verify(auditService, never()).log(eq(EventType.RULE_EXECUTION), eq("T1"), anyMap());
    }

    @Test
    void skipsRulesWithBrokenConfig() {
        when(ruleRepository.findByEnabledTrue()).thenReturn(List.of(new TaxRule("Broken", "ALWAYS", "{not json", true)));

        assertEquals(0, engine.execute(txn));
        verify(exceptionService, never()).raise(eq("T1"), eq("C1"), eq("Broken"), eq(Severity.LOW), eq("boom"));
    }

    @Test
    void doesNothingWhenNoActiveRules() {
        when(ruleRepository.findByEnabledTrue()).thenReturn(List.of());
        assertEquals(0, engine.execute(txn));
    }
}
