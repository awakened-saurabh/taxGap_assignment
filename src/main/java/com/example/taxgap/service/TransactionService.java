package com.example.taxgap.service;

import com.example.taxgap.domain.ComplianceStatus;
import com.example.taxgap.domain.FinancialTransaction;
import com.example.taxgap.domain.EventType;
import com.example.taxgap.domain.Severity;
import com.example.taxgap.domain.TaxComputation;
import com.example.taxgap.domain.TransactionType;
import com.example.taxgap.domain.ValidationStatus;
import com.example.taxgap.dto.TaxResult;
import com.example.taxgap.dto.TransactionRequest;
import com.example.taxgap.dto.TransactionResult;
import com.example.taxgap.dto.UploadResponse;
import com.example.taxgap.repository.FinancialTransactionRepository;
import com.example.taxgap.repository.TaxComputationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.example.taxgap.service.AuditService.details;

@Service
@RequiredArgsConstructor
public class TransactionService {

    static final String TAX_GAP_RULE = "TAX_GAP_CHECK";

    private final FinancialTransactionRepository transactionRepository;
    private final TransactionValidator validator;
    private final TaxCalculationService taxCalculationService;
    private final TaxComputationRepository computationRepository;
    private final RuleEngineService ruleEngineService;
    private final ExceptionService exceptionService;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    @Transactional
    public UploadResponse upload(List<TransactionRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("Batch must contain at least one transaction");
        }

        List<TransactionResult> results = requests.stream()
                .filter(Objects::nonNull)
                .map(this::process)
                .toList();

        long success = results.stream().filter(r -> r.validationStatus() == ValidationStatus.SUCCESS).count();
        return new UploadResponse(results.size(), success, results.size() - success, results);
    }

    private TransactionResult process(TransactionRequest request) {
        FinancialTransaction txn = new FinancialTransaction();
        txn.setTransactionId(trim(request.transactionId()));
        txn.setCustomerId(trim(request.customerId()));
        txn.setRawPayload(toJson(request));
        txn.setCreatedAt(LocalDateTime.now());

        List<String> errors = validator.validate(request);
        if (errors.isEmpty()
                && transactionRepository.existsByTransactionIdAndValidationStatus(txn.getTransactionId(), ValidationStatus.SUCCESS)) {
            errors.add("Duplicate transactionId " + txn.getTransactionId());
        }

        if (!errors.isEmpty()) {
            txn.setValidationStatus(ValidationStatus.FAILURE);
            txn.setFailureReasons(String.join("; ", errors));
            transactionRepository.save(txn);
            auditService.log(EventType.INGESTION, txn.getTransactionId(),
                    details("status", ValidationStatus.FAILURE, "reasons", errors, "old", null, "new", request));
            return new TransactionResult(txn.getTransactionId(), ValidationStatus.FAILURE, errors, null, 0);
        }

        txn.setDate(LocalDate.parse(request.date().trim()));
        txn.setAmount(request.amount());
        txn.setTaxRate(request.taxRate());
        txn.setReportedTax(request.reportedTax());
        txn.setTransactionType(TransactionType.valueOf(request.transactionType().trim().toUpperCase()));
        txn.setOriginalTransactionId(trim(request.originalTransactionId()));
        txn.setValidationStatus(ValidationStatus.SUCCESS);
        transactionRepository.save(txn);
        auditService.log(EventType.INGESTION, txn.getTransactionId(),
                details("status", ValidationStatus.SUCCESS, "old", null, "new", request));

        TaxResult result = taxCalculationService.calculate(txn.getAmount(), txn.getTaxRate(), txn.getReportedTax());
        TaxComputation computation = new TaxComputation();
        computation.setTransaction(txn);
        computation.setExpectedTax(result.expectedTax());
        computation.setTaxGap(result.taxGap());
        computation.setComplianceStatus(result.status());
        computationRepository.save(computation);
        auditService.log(EventType.TAX_COMPUTATION, txn.getTransactionId(),
                details("amount", txn.getAmount(), "taxRate", txn.getTaxRate(), "reportedTax", txn.getReportedTax(),
                        "expectedTax", result.expectedTax(), "taxGap", result.taxGap(), "status", result.status()));

        int raised = 0;
        if (result.status() != ComplianceStatus.COMPLIANT) {
            exceptionService.raise(txn.getTransactionId(), txn.getCustomerId(), TAX_GAP_RULE,
                    severityFor(result.status()), gapMessage(result));
            raised++;
        }
        raised += ruleEngineService.execute(txn);

        return new TransactionResult(txn.getTransactionId(), ValidationStatus.SUCCESS, List.of(), result.status(), raised);
    }

    private Severity severityFor(ComplianceStatus status) {
        return switch (status) {
            case UNDERPAID -> Severity.HIGH;
            case NON_COMPLIANT -> Severity.MEDIUM;
            default -> Severity.LOW;
        };
    }

    private String gapMessage(TaxResult result) {
        return switch (result.status()) {
            case UNDERPAID -> "Tax underpaid by " + result.taxGap() + " (expected " + result.expectedTax() + ")";
            case OVERPAID -> "Tax overpaid by " + result.taxGap().abs() + " (expected " + result.expectedTax() + ")";
            default -> "Reported tax is missing (expected " + result.expectedTax() + ")";
        };
    }

    private String trim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String toJson(TransactionRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            return String.valueOf(request);
        }
    }
}
