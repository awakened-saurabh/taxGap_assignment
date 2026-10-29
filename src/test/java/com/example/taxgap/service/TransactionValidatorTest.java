package com.example.taxgap.service;

import com.example.taxgap.TestData;
import com.example.taxgap.dto.TransactionRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionValidatorTest {

    private final TransactionValidator validator = new TransactionValidator();

    @Test
    void validRequestHasNoErrors() {
        assertTrue(validator.validate(TestData.validRequest()).isEmpty());
    }

    @Test
    void reportedTaxIsOptional() {
        TransactionRequest r = new TransactionRequest("T1", "2026-10-01", "C1",
                new BigDecimal("100"), new BigDecimal("0.18"), null, "SALE", null);
        assertTrue(validator.validate(r).isEmpty());
    }

    @Test
    void missingRequiredFieldsAreReported() {
        TransactionRequest r = new TransactionRequest(null, null, " ", null, null, null, null, null);
        List<String> errors = validator.validate(r);
        assertEquals(6, errors.size());
        assertTrue(errors.contains("transactionId is required"));
        assertTrue(errors.contains("customerId is required"));
        assertTrue(errors.contains("date is required"));
        assertTrue(errors.contains("amount is required"));
        assertTrue(errors.contains("taxRate is required"));
        assertTrue(errors.contains("transactionType is required"));
    }

    @Test
    void amountMustBePositive() {
        TransactionRequest zero = new TransactionRequest("T1", "2026-10-01", "C1",
                BigDecimal.ZERO, new BigDecimal("0.18"), null, "SALE", null);
        TransactionRequest negative = new TransactionRequest("T1", "2026-10-01", "C1",
                new BigDecimal("-5"), new BigDecimal("0.18"), null, "SALE", null);
        assertTrue(validator.validate(zero).contains("amount must be greater than 0"));
        assertTrue(validator.validate(negative).contains("amount must be greater than 0"));
    }

    @Test
    void invalidDateFormatIsRejected() {
        for (String date : List.of("01-10-2026", "2026/10/01", "2026-13-45", "2026-02-30", "abc")) {
            TransactionRequest r = new TransactionRequest("T1", date, "C1",
                    new BigDecimal("100"), new BigDecimal("0.18"), null, "SALE", null);
            assertTrue(validator.validate(r).contains("date must be a valid date in yyyy-MM-dd format"), date);
        }
    }

    @Test
    void unknownTransactionTypeIsRejected() {
        TransactionRequest r = new TransactionRequest("T1", "2026-10-01", "C1",
                new BigDecimal("100"), new BigDecimal("0.18"), null, "GIFT", null);
        assertTrue(validator.validate(r).contains("transactionType must be one of SALE, REFUND, EXPENSE"));
    }

    @Test
    void transactionTypeIsCaseInsensitive() {
        TransactionRequest r = new TransactionRequest("T1", "2026-10-01", "C1",
                new BigDecimal("100"), new BigDecimal("0.18"), null, "refund", null);
        assertTrue(validator.validate(r).isEmpty());
    }

    @Test
    void negativeRateAndReportedTaxAreRejected() {
        TransactionRequest r = new TransactionRequest("T1", "2026-10-01", "C1",
                new BigDecimal("100"), new BigDecimal("-0.1"), new BigDecimal("-1"), "SALE", null);
        List<String> errors = validator.validate(r);
        assertTrue(errors.contains("taxRate must not be negative"));
        assertTrue(errors.contains("reportedTax must not be negative"));
    }
}
