package com.example.taxgap.rules;

import com.example.taxgap.TestData;
import com.example.taxgap.domain.Severity;
import com.example.taxgap.domain.TransactionType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HighValueRuleTest {

    private final HighValueRule rule = new HighValueRule();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void flagsAmountAboveThreshold() throws Exception {
        JsonNode config = mapper.readTree("{\"threshold\": 1000000, \"severity\": \"HIGH\"}");
        Optional<RuleViolation> result = rule.evaluate(
                TestData.txn("T1", "C1", "1500000", "0.18", TransactionType.SALE), config);
        assertTrue(result.isPresent());
        assertEquals(Severity.HIGH, result.get().severity());
    }

    @Test
    void doesNotFlagAmountEqualToThreshold() throws Exception {
        JsonNode config = mapper.readTree("{\"threshold\": 1000000}");
        assertTrue(rule.evaluate(TestData.txn("T1", "C1", "1000000", "0.18", TransactionType.SALE), config).isEmpty());
    }

    @Test
    void usesConfiguredSeverity() throws Exception {
        JsonNode config = mapper.readTree("{\"threshold\": 100, \"severity\": \"LOW\"}");
        Optional<RuleViolation> result = rule.evaluate(
                TestData.txn("T1", "C1", "500", "0.18", TransactionType.SALE), config);
        assertEquals(Severity.LOW, result.orElseThrow().severity());
    }

    @Test
    void fallsBackToHighWhenSeverityIsInvalid() throws Exception {
        JsonNode config = mapper.readTree("{\"threshold\": 100, \"severity\": \"EXTREME\"}");
        Optional<RuleViolation> result = rule.evaluate(
                TestData.txn("T1", "C1", "500", "0.18", TransactionType.SALE), config);
        assertEquals(Severity.HIGH, result.orElseThrow().severity());
    }

    @Test
    void ignoresConfigWithoutThreshold() throws Exception {
        JsonNode config = mapper.readTree("{}");
        assertTrue(rule.evaluate(TestData.txn("T1", "C1", "9999999", "0.18", TransactionType.SALE), config).isEmpty());
    }
}
