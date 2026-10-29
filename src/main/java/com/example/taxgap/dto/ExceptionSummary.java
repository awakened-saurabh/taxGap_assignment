package com.example.taxgap.dto;

import java.util.Map;

public record ExceptionSummary(long totalExceptions, Map<String, Long> bySeverity, Map<String, Long> byCustomer) {
}
