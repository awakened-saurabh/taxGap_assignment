package com.example.taxgap.rules;

import com.example.taxgap.domain.Severity;

public record RuleViolation(Severity severity, String message) {
}
