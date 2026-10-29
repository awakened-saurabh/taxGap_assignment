package com.example.taxgap.service;

import com.example.taxgap.domain.Severity;
import com.example.taxgap.dto.CustomerTaxSummary;
import com.example.taxgap.dto.ExceptionSummary;
import com.example.taxgap.repository.TaxComputationRepository;
import com.example.taxgap.repository.TaxExceptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final TaxComputationRepository computationRepository;
    private final TaxExceptionRepository exceptionRepository;

    public List<CustomerTaxSummary> customerSummary() {
        return computationRepository.customerSummary();
    }

    public ExceptionSummary exceptionSummary() {
        Map<String, Long> bySeverity = new LinkedHashMap<>();
        for (Severity severity : Severity.values()) {
            bySeverity.put(severity.name(), 0L);
        }
        for (Object[] row : exceptionRepository.severityCounts()) {
            bySeverity.put(((Severity) row[0]).name(), ((Number) row[1]).longValue());
        }

        Map<String, Long> byCustomer = new LinkedHashMap<>();
        for (Object[] row : exceptionRepository.customerCounts()) {
            byCustomer.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }

        long total = bySeverity.values().stream().mapToLong(Long::longValue).sum();
        return new ExceptionSummary(total, bySeverity, byCustomer);
    }
}
