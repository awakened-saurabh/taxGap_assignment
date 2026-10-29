package com.example.taxgap.service;

import com.example.taxgap.domain.ComplianceStatus;
import com.example.taxgap.dto.TaxResult;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TaxCalculationServiceTest {

    private final TaxCalculationService service = new TaxCalculationService();

    private TaxResult calc(String amount, String rate, String reported) {
        return service.calculate(new BigDecimal(amount), new BigDecimal(rate),
                reported == null ? null : new BigDecimal(reported));
    }

    @Test
    void exactMatchIsCompliant() {
        TaxResult result = calc("1000", "0.18", "180");
        assertEquals(0, new BigDecimal("180.00").compareTo(result.expectedTax()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.taxGap()));
        assertEquals(ComplianceStatus.COMPLIANT, result.status());
    }

    @Test
    void gapOfExactlyOneIsStillCompliant() {
        assertEquals(ComplianceStatus.COMPLIANT, calc("1000", "0.18", "179").status());
        assertEquals(ComplianceStatus.COMPLIANT, calc("1000", "0.18", "181").status());
    }

    @Test
    void gapAboveOneIsUnderpaid() {
        TaxResult result = calc("1000", "0.18", "150");
        assertEquals(ComplianceStatus.UNDERPAID, result.status());
        assertEquals(0, new BigDecimal("30.00").compareTo(result.taxGap()));
    }

    @Test
    void gapBelowMinusOneIsOverpaid() {
        TaxResult result = calc("1000", "0.18", "200");
        assertEquals(ComplianceStatus.OVERPAID, result.status());
        assertEquals(0, new BigDecimal("-20.00").compareTo(result.taxGap()));
    }

    @Test
    void missingReportedTaxIsNonCompliant() {
        TaxResult result = calc("1000", "0.18", null);
        assertEquals(ComplianceStatus.NON_COMPLIANT, result.status());
        assertEquals(0, new BigDecimal("180.00").compareTo(result.expectedTax()));
    }

    @Test
    void expectedTaxIsRoundedToTwoDecimals() {
        TaxResult result = calc("333.33", "0.18", "60");
        assertEquals(new BigDecimal("60.00"), result.expectedTax());
    }
}
