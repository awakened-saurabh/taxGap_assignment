package com.example.taxgap.service;

import com.example.taxgap.domain.Severity;
import com.example.taxgap.dto.CustomerTaxSummary;
import com.example.taxgap.dto.ExceptionSummary;
import com.example.taxgap.repository.TaxComputationRepository;
import com.example.taxgap.repository.TaxExceptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReportServiceTest {

    @Mock
    private TaxComputationRepository computationRepository;
    @Mock
    private TaxExceptionRepository exceptionRepository;
    @InjectMocks
    private ReportService service;

    @Test
    void complianceScoreReflectsNonCompliantShare() {
        CustomerTaxSummary summary = new CustomerTaxSummary("C1", new BigDecimal("1000"), new BigDecimal("100"),
                new BigDecimal("180"), new BigDecimal("80"), 4L, 1L);
        assertEquals(new BigDecimal("75.00"), summary.getComplianceScore());
    }

    @Test
    void complianceScoreIsHundredWhenAllCompliant() {
        CustomerTaxSummary summary = new CustomerTaxSummary("C1", BigDecimal.TEN, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ZERO, 3L, 0L);
        assertEquals(new BigDecimal("100.00"), summary.getComplianceScore());
    }

    @Test
    void nullSumsAreTreatedAsZero() {
        CustomerTaxSummary summary = new CustomerTaxSummary("C1", BigDecimal.TEN, null,
                BigDecimal.ONE, BigDecimal.ONE, 1L, 1L);
        assertEquals(BigDecimal.ZERO, summary.getTotalReportedTax());
        assertEquals(new BigDecimal("0.00"), summary.getComplianceScore());
    }

    @Test
    void customerSummaryDelegatesToRepository() {
        CustomerTaxSummary summary = new CustomerTaxSummary("C1", BigDecimal.TEN, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ZERO, 1L, 0L);
        when(computationRepository.customerSummary()).thenReturn(List.of(summary));

        assertEquals(1, service.customerSummary().size());
        assertEquals("C1", service.customerSummary().get(0).getCustomerId());
    }

    @Test
    void exceptionSummaryCountsBySeverityAndCustomer() {
        when(exceptionRepository.severityCounts()).thenReturn(List.of(
                new Object[]{Severity.HIGH, 3L}, new Object[]{Severity.LOW, 1L}));
        when(exceptionRepository.customerCounts()).thenReturn(List.of(
                new Object[]{"C1", 3L}, new Object[]{"C2", 1L}));

        ExceptionSummary summary = service.exceptionSummary();

        assertEquals(4, summary.totalExceptions());
        assertEquals(3L, summary.bySeverity().get("HIGH"));
        assertEquals(0L, summary.bySeverity().get("MEDIUM"));
        assertEquals(1L, summary.bySeverity().get("LOW"));
        assertEquals(3L, summary.byCustomer().get("C1"));
        assertEquals(1L, summary.byCustomer().get("C2"));
    }
}
