package com.example.taxgap.service;

import com.example.taxgap.TestData;
import com.example.taxgap.domain.ComplianceStatus;
import com.example.taxgap.domain.EventType;
import com.example.taxgap.domain.FinancialTransaction;
import com.example.taxgap.domain.Severity;
import com.example.taxgap.domain.TaxComputation;
import com.example.taxgap.domain.ValidationStatus;
import com.example.taxgap.dto.TransactionRequest;
import com.example.taxgap.dto.TransactionResult;
import com.example.taxgap.dto.UploadResponse;
import com.example.taxgap.repository.FinancialTransactionRepository;
import com.example.taxgap.repository.TaxComputationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransactionServiceTest {

    @Mock
    private FinancialTransactionRepository transactionRepository;
    @Mock
    private TaxComputationRepository computationRepository;
    @Mock
    private RuleEngineService ruleEngineService;
    @Mock
    private ExceptionService exceptionService;
    @Mock
    private AuditService auditService;

    private TransactionService service;

    @BeforeEach
    void setUp() {
        service = new TransactionService(transactionRepository, new TransactionValidator(),
                new TaxCalculationService(), computationRepository, ruleEngineService,
                exceptionService, auditService, new ObjectMapper());
        when(transactionRepository.save(any(FinancialTransaction.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void validCompliantTransactionIsStoredAndProcessed() {
        UploadResponse response = service.upload(List.of(TestData.validRequest()));

        assertEquals(1, response.total());
        assertEquals(1, response.success());
        assertEquals(0, response.failure());
        TransactionResult result = response.results().get(0);
        assertEquals(ComplianceStatus.COMPLIANT, result.complianceStatus());
        verify(computationRepository).save(any(TaxComputation.class));
        verify(ruleEngineService).execute(any(FinancialTransaction.class));
        verify(exceptionService, never()).raise(anyString(), anyString(), anyString(), any(), anyString());
        verify(auditService).log(eq(EventType.INGESTION), eq("T1001"), anyMap());
        verify(auditService).log(eq(EventType.TAX_COMPUTATION), eq("T1001"), anyMap());
    }

    @Test
    void underpaidTransactionRaisesTaxGapException() {
        TransactionRequest request = new TransactionRequest("T1001", "2026-10-01", "C001",
                new BigDecimal("1000"), new BigDecimal("0.18"), new BigDecimal("100"), "SALE", null);

        UploadResponse response = service.upload(List.of(request));

        assertEquals(ComplianceStatus.UNDERPAID, response.results().get(0).complianceStatus());
        assertEquals(1, response.results().get(0).exceptionsRaised());
        verify(exceptionService).raise(eq("T1001"), eq("C001"), eq("TAX_GAP_CHECK"), eq(Severity.HIGH), anyString());
    }

    @Test
    void missingReportedTaxIsFlaggedNonCompliant() {
        TransactionRequest request = new TransactionRequest("T1001", "2026-10-01", "C001",
                new BigDecimal("1000"), new BigDecimal("0.18"), null, "SALE", null);

        UploadResponse response = service.upload(List.of(request));

        assertEquals(ComplianceStatus.NON_COMPLIANT, response.results().get(0).complianceStatus());
        verify(exceptionService).raise(eq("T1001"), eq("C001"), eq("TAX_GAP_CHECK"), eq(Severity.MEDIUM), anyString());
    }

    @Test
    void ruleEngineExceptionsAreIncludedInCount() {
        when(ruleEngineService.execute(any(FinancialTransaction.class))).thenReturn(2);

        UploadResponse response = service.upload(List.of(TestData.validRequest()));

        assertEquals(2, response.results().get(0).exceptionsRaised());
    }

    @Test
    void invalidTransactionIsStoredAsFailureWithReasons() {
        TransactionRequest request = new TransactionRequest("T2", "31-12-2026", "C001",
                new BigDecimal("-10"), new BigDecimal("0.18"), null, "SALE", null);

        UploadResponse response = service.upload(List.of(request));

        assertEquals(1, response.failure());
        TransactionResult result = response.results().get(0);
        assertEquals(ValidationStatus.FAILURE, result.validationStatus());
        assertEquals(2, result.failureReasons().size());

        ArgumentCaptor<FinancialTransaction> captor = ArgumentCaptor.forClass(FinancialTransaction.class);
        verify(transactionRepository).save(captor.capture());
        assertEquals(ValidationStatus.FAILURE, captor.getValue().getValidationStatus());
        assertTrue(captor.getValue().getRawPayload().contains("31-12-2026"));
        verify(computationRepository, never()).save(any(TaxComputation.class));
        verify(ruleEngineService, never()).execute(any(FinancialTransaction.class));
        verify(auditService).log(eq(EventType.INGESTION), eq("T2"), anyMap());
    }

    @Test
    void duplicateTransactionIdIsRejected() {
        when(transactionRepository.existsByTransactionIdAndValidationStatus("T1001", ValidationStatus.SUCCESS))
                .thenReturn(true);

        UploadResponse response = service.upload(List.of(TestData.validRequest()));

        assertEquals(1, response.failure());
        assertTrue(response.results().get(0).failureReasons().get(0).contains("Duplicate"));
    }

    @Test
    void mixedBatchIsProcessedRecordByRecord() {
        TransactionRequest bad = new TransactionRequest("T2", "bad-date", "C1",
                new BigDecimal("10"), new BigDecimal("0.18"), null, "SALE", null);

        UploadResponse response = service.upload(List.of(TestData.validRequest(), bad));

        assertEquals(2, response.total());
        assertEquals(1, response.success());
        assertEquals(1, response.failure());
        verify(transactionRepository, times(2)).save(any(FinancialTransaction.class));
    }

    @Test
    void emptyBatchIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> service.upload(List.of()));
        assertThrows(IllegalArgumentException.class, () -> service.upload(null));
    }
}
