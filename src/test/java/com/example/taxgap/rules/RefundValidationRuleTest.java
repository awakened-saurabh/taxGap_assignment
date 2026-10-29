package com.example.taxgap.rules;

import com.example.taxgap.TestData;
import com.example.taxgap.domain.FinancialTransaction;
import com.example.taxgap.domain.Severity;
import com.example.taxgap.domain.TransactionType;
import com.example.taxgap.domain.ValidationStatus;
import com.example.taxgap.repository.FinancialTransactionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RefundValidationRuleTest {

    @Mock
    private FinancialTransactionRepository repository;

    private RefundValidationRule rule;
    private JsonNode config;

    @BeforeEach
    void setUp() throws Exception {
        rule = new RefundValidationRule(repository);
        config = new ObjectMapper().readTree("{\"severity\": \"HIGH\"}");
    }

    @Test
    void ignoresNonRefundTransactions() {
        assertTrue(rule.evaluate(TestData.txn("T1", "C1", "500", "0.18", TransactionType.SALE), config).isEmpty());
    }

    @Test
    void flagsRefundAboveLinkedSale() {
        FinancialTransaction sale = TestData.txn("S1", "C1", "1000", "0.18", TransactionType.SALE);
        when(repository.findFirstByTransactionIdAndValidationStatus("S1", ValidationStatus.SUCCESS))
                .thenReturn(Optional.of(sale));
        FinancialTransaction refund = TestData.txn("R1", "C1", "1500", "0.18", TransactionType.REFUND);
        refund.setOriginalTransactionId("S1");

        Optional<RuleViolation> result = rule.evaluate(refund, config);
        assertEquals(Severity.HIGH, result.orElseThrow().severity());
        assertTrue(result.get().message().contains("exceeds original sale amount"));
    }

    @Test
    void acceptsRefundWithinLinkedSale() {
        FinancialTransaction sale = TestData.txn("S1", "C1", "1000", "0.18", TransactionType.SALE);
        when(repository.findFirstByTransactionIdAndValidationStatus("S1", ValidationStatus.SUCCESS))
                .thenReturn(Optional.of(sale));
        FinancialTransaction refund = TestData.txn("R1", "C1", "1000", "0.18", TransactionType.REFUND);
        refund.setOriginalTransactionId("S1");

        assertTrue(rule.evaluate(refund, config).isEmpty());
    }

    @Test
    void flagsRefundWhenLinkedSaleDoesNotExist() {
        when(repository.findFirstByTransactionIdAndValidationStatus("S9", ValidationStatus.SUCCESS))
                .thenReturn(Optional.empty());
        FinancialTransaction refund = TestData.txn("R1", "C1", "100", "0.18", TransactionType.REFUND);
        refund.setOriginalTransactionId("S9");

        assertTrue(rule.evaluate(refund, config).orElseThrow().message().contains("not found"));
    }

    @Test
    void flagsRefundWhenLinkedTransactionIsNotASale() {
        FinancialTransaction expense = TestData.txn("E1", "C1", "1000", "0.18", TransactionType.EXPENSE);
        when(repository.findFirstByTransactionIdAndValidationStatus("E1", ValidationStatus.SUCCESS))
                .thenReturn(Optional.of(expense));
        FinancialTransaction refund = TestData.txn("R1", "C1", "100", "0.18", TransactionType.REFUND);
        refund.setOriginalTransactionId("E1");

        assertTrue(rule.evaluate(refund, config).isPresent());
    }

    @Test
    void withoutLinkComparesAgainstCustomerSalesTotal() {
        when(repository.sumAmount("C1", TransactionType.SALE, ValidationStatus.SUCCESS))
                .thenReturn(new BigDecimal("1000"));

        assertTrue(rule.evaluate(TestData.txn("R1", "C1", "1200", "0.18", TransactionType.REFUND), config).isPresent());
        assertTrue(rule.evaluate(TestData.txn("R2", "C1", "900", "0.18", TransactionType.REFUND), config).isEmpty());
    }

    @Test
    void withoutLinkAndNoSalesIsFlagged() {
        when(repository.sumAmount("C1", TransactionType.SALE, ValidationStatus.SUCCESS)).thenReturn(null);
        assertTrue(rule.evaluate(TestData.txn("R1", "C1", "10", "0.18", TransactionType.REFUND), config).isPresent());
    }
}
