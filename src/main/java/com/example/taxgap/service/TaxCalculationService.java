package com.example.taxgap.service;

import com.example.taxgap.domain.ComplianceStatus;
import com.example.taxgap.dto.TaxResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class TaxCalculationService {

    private static final BigDecimal TOLERANCE = BigDecimal.ONE;

    public TaxResult calculate(BigDecimal amount, BigDecimal taxRate, BigDecimal reportedTax) {
        BigDecimal expectedTax = amount.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal reported = reportedTax == null ? BigDecimal.ZERO : reportedTax;
        BigDecimal taxGap = expectedTax.subtract(reported);

        ComplianceStatus status;
        if (reportedTax == null) {
            status = ComplianceStatus.NON_COMPLIANT;
        } else if (taxGap.abs().compareTo(TOLERANCE) <= 0) {
            status = ComplianceStatus.COMPLIANT;
        } else if (taxGap.signum() > 0) {
            status = ComplianceStatus.UNDERPAID;
        } else {
            status = ComplianceStatus.OVERPAID;
        }
        return new TaxResult(expectedTax, taxGap, status);
    }
}
