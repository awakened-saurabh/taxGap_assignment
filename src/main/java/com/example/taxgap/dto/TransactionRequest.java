package com.example.taxgap.dto;

import java.math.BigDecimal;

public record TransactionRequest(
        String transactionId,
        String date,
        String customerId,
        BigDecimal amount,
        BigDecimal taxRate,
        BigDecimal reportedTax,
        String transactionType,
        String originalTransactionId) {
}
