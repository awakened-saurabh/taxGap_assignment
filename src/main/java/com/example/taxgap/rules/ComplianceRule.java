package com.example.taxgap.rules;

import com.example.taxgap.domain.FinancialTransaction;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

public interface ComplianceRule {

    String type();

    Optional<RuleViolation> evaluate(FinancialTransaction txn, JsonNode config);
}
