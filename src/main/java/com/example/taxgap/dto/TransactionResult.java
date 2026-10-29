package com.example.taxgap.dto;

import com.example.taxgap.domain.ComplianceStatus;
import com.example.taxgap.domain.ValidationStatus;

import java.util.List;

public record TransactionResult(
        String transactionId,
        ValidationStatus validationStatus,
        List<String> failureReasons,
        ComplianceStatus complianceStatus,
        int exceptionsRaised) {
}
