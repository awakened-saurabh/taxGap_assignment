package com.example.taxgap.dto;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
public class CustomerTaxSummary {

    private final String customerId;
    private final BigDecimal totalAmount;
    private final BigDecimal totalReportedTax;
    private final BigDecimal totalExpectedTax;
    private final BigDecimal totalTaxGap;
    private final long totalTransactions;
    private final long nonCompliantTransactions;
    private final BigDecimal complianceScore;

    public CustomerTaxSummary(String customerId, BigDecimal totalAmount, BigDecimal totalReportedTax,
                              BigDecimal totalExpectedTax, BigDecimal totalTaxGap,
                              Number totalTransactions, Number nonCompliantTransactions) {
        this.customerId = customerId;
        this.totalAmount = orZero(totalAmount);
        this.totalReportedTax = orZero(totalReportedTax);
        this.totalExpectedTax = orZero(totalExpectedTax);
        this.totalTaxGap = orZero(totalTaxGap);
        this.totalTransactions = totalTransactions.longValue();
        this.nonCompliantTransactions = nonCompliantTransactions.longValue();
        this.complianceScore = score(this.totalTransactions, this.nonCompliantTransactions);
    }

    private static BigDecimal orZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static BigDecimal score(long total, long nonCompliant) {
        if (total == 0) {
            return BigDecimal.valueOf(100).setScale(2);
        }
        BigDecimal ratio = BigDecimal.valueOf(nonCompliant)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
        return BigDecimal.valueOf(100).subtract(ratio).setScale(2, RoundingMode.HALF_UP);
    }
}
