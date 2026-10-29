package com.example.taxgap.rules;

import com.example.taxgap.domain.Severity;
import com.fasterxml.jackson.databind.JsonNode;

final class RuleConfig {

    private RuleConfig() {
    }

    static Severity severity(JsonNode config, Severity fallback) {
        if (config == null || !config.hasNonNull("severity")) {
            return fallback;
        }
        try {
            return Severity.valueOf(config.get("severity").asText().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
