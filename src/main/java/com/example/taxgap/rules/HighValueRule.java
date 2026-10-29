package com.example.taxgap.rules;

import com.example.taxgap.domain.FinancialTransaction;
import com.example.taxgap.domain.Severity;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class HighValueRule implements ComplianceRule {

    public static final String TYPE = "HIGH_VALUE";

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public Optional<RuleViolation> evaluate(FinancialTransaction txn, JsonNode config) {
        if (config == null || !config.hasNonNull("threshold")) {
            return Optional.empty();
        }
        BigDecimal threshold = config.get("threshold").decimalValue();
        if (txn.getAmount().compareTo(threshold) > 0) {
            return Optional.of(new RuleViolation(RuleConfig.severity(config, Severity.HIGH),
                    "Amount " + txn.getAmount() + " exceeds high-value threshold " + threshold));
        }
        return Optional.empty();
    }
}
