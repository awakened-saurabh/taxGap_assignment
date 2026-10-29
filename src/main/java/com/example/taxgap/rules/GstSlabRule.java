package com.example.taxgap.rules;

import com.example.taxgap.domain.FinancialTransaction;
import com.example.taxgap.domain.Severity;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Component
public class GstSlabRule implements ComplianceRule {

    public static final String TYPE = "GST_SLAB";

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public Optional<RuleViolation> evaluate(FinancialTransaction txn, JsonNode config) {
        if (config == null || !config.has("slabs") || !config.get("slabs").isArray()) {
            return Optional.empty();
        }

        BigDecimal matchedAbove = null;
        BigDecimal requiredRate = null;
        for (JsonNode slab : config.get("slabs")) {
            if (!slab.hasNonNull("above") || !slab.hasNonNull("minRate")) {
                continue;
            }
            BigDecimal above = slab.get("above").decimalValue();
            if (txn.getAmount().compareTo(above) > 0 && (matchedAbove == null || above.compareTo(matchedAbove) > 0)) {
                matchedAbove = above;
                requiredRate = slab.get("minRate").decimalValue();
            }
        }

        if (requiredRate != null && txn.getTaxRate().compareTo(requiredRate) < 0) {
            return Optional.of(new RuleViolation(RuleConfig.severity(config, Severity.MEDIUM),
                    "Amount " + txn.getAmount() + " is above slab threshold " + matchedAbove
                            + " which needs a tax rate of at least " + requiredRate
                            + ", but " + txn.getTaxRate() + " was applied"));
        }
        return Optional.empty();
    }
}
