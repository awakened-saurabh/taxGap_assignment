package com.example.taxgap;

import com.example.taxgap.domain.FinancialTransaction;
import com.example.taxgap.domain.TransactionType;
import com.example.taxgap.domain.ValidationStatus;
import com.example.taxgap.dto.TransactionRequest;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class TestData {

    private TestData() {
    }

    public static FinancialTransaction txn(String id, String customerId, String amount, String taxRate, TransactionType type) {
        FinancialTransaction t = new FinancialTransaction();
        t.setTransactionId(id);
        t.setCustomerId(customerId);
        t.setDate(LocalDate.of(2026, 10, 1));
        t.setAmount(new BigDecimal(amount));
        t.setTaxRate(new BigDecimal(taxRate));
        t.setTransactionType(type);
        t.setValidationStatus(ValidationStatus.SUCCESS);
        return t;
    }

    public static TransactionRequest validRequest() {
        return new TransactionRequest("T1001", "2026-10-01", "C001",
                new BigDecimal("1000"), new BigDecimal("0.18"), new BigDecimal("180"), "SALE", null);
    }
}
