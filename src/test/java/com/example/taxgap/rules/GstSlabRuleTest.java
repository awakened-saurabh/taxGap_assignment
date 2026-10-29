package com.example.taxgap.rules;

import com.example.taxgap.TestData;
import com.example.taxgap.domain.Severity;
import com.example.taxgap.domain.TransactionType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GstSlabRuleTest {

    private final GstSlabRule rule = new GstSlabRule();
    private JsonNode config;

    @BeforeEach
    void setUp() throws Exception {
        config = new ObjectMapper().readTree("""
                {"slabs": [{"above": 50000, "minRate": 0.12}, {"above": 100000, "minRate": 0.18}], "severity": "MEDIUM"}
                """);
    }

    @Test
    void flagsLowRateInTopSlab() {
        Optional<RuleViolation> result = rule.evaluate(
                TestData.txn("T1", "C1", "120000", "0.12", TransactionType.SALE), config);
        assertTrue(result.isPresent());
        assertEquals(Severity.MEDIUM, result.get().severity());
        assertTrue(result.get().message().contains("0.18"));
    }

    @Test
    void acceptsRequiredRateInTopSlab() {
        assertTrue(rule.evaluate(TestData.txn("T1", "C1", "120000", "0.18", TransactionType.SALE), config).isEmpty());
    }

    @Test
    void flagsLowRateInMiddleSlab() {
        Optional<RuleViolation> result = rule.evaluate(
                TestData.txn("T1", "C1", "60000", "0.05", TransactionType.SALE), config);
        assertTrue(result.orElseThrow().message().contains("0.12"));
    }

    @Test
    void amountBelowAllSlabsIsIgnored() {
        assertTrue(rule.evaluate(TestData.txn("T1", "C1", "40000", "0.05", TransactionType.SALE), config).isEmpty());
    }

    @Test
    void amountEqualToSlabThresholdIsNotAboveIt() {
        assertTrue(rule.evaluate(TestData.txn("T1", "C1", "50000", "0.05", TransactionType.SALE), config).isEmpty());
    }

    @Test
    void missingSlabsInConfigIsIgnored() throws Exception {
        JsonNode empty = new ObjectMapper().readTree("{}");
        assertTrue(rule.evaluate(TestData.txn("T1", "C1", "120000", "0.01", TransactionType.SALE), empty).isEmpty());
    }
}
