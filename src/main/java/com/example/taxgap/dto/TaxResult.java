package com.example.taxgap.dto;

import com.example.taxgap.domain.ComplianceStatus;

import java.math.BigDecimal;

public record TaxResult(BigDecimal expectedTax, BigDecimal taxGap, ComplianceStatus status) {
}
