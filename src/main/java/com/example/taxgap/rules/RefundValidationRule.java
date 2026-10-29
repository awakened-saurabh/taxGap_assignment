package com.example.taxgap.rules;

import com.example.taxgap.domain.FinancialTransaction;
import com.example.taxgap.domain.Severity;
import com.example.taxgap.domain.TransactionType;
import com.example.taxgap.domain.ValidationStatus;
import com.example.taxgap.repository.FinancialTransactionRepository;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RefundValidationRule implements ComplianceRule {

    public static final String TYPE = "REFUND_VALIDATION";

    private final FinancialTransactionRepository transactionRepository;

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public Optional<RuleViolation> evaluate(FinancialTransaction txn, JsonNode config) {
        if (txn.getTransactionType() != TransactionType.REFUND) {
            return Optional.empty();
        }
        Severity severity = RuleConfig.severity(config, Severity.HIGH);

        if (txn.getOriginalTransactionId() != null) {
            Optional<FinancialTransaction> sale = transactionRepository
                    .findFirstByTransactionIdAndValidationStatus(txn.getOriginalTransactionId(), ValidationStatus.SUCCESS);
            if (sale.isEmpty() || sale.get().getTransactionType() != TransactionType.SALE) {
                return Optional.of(new RuleViolation(severity,
                        "Original sale " + txn.getOriginalTransactionId() + " not found for refund"));
            }
            if (txn.getAmount().compareTo(sale.get().getAmount()) > 0) {
                return Optional.of(new RuleViolation(severity,
                        "Refund amount " + txn.getAmount() + " exceeds original sale amount " + sale.get().getAmount()));
            }
            return Optional.empty();
        }

        BigDecimal totalSales = transactionRepository.sumAmount(
                txn.getCustomerId(), TransactionType.SALE, ValidationStatus.SUCCESS);
        if (totalSales == null) {
            totalSales = BigDecimal.ZERO;
        }
        if (txn.getAmount().compareTo(totalSales) > 0) {
            return Optional.of(new RuleViolation(severity,
                    "Refund amount " + txn.getAmount() + " exceeds total sales " + totalSales
                            + " recorded for customer " + txn.getCustomerId()));
        }
        return Optional.empty();
    }
}
