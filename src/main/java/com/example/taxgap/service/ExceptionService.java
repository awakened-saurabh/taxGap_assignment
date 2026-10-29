package com.example.taxgap.service;

import com.example.taxgap.domain.Severity;
import com.example.taxgap.domain.TaxException;
import com.example.taxgap.repository.TaxExceptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExceptionService {

    private final TaxExceptionRepository repository;

    public TaxException raise(String transactionId, String customerId, String ruleName,
                              Severity severity, String message) {
        TaxException exception = new TaxException();
        exception.setTransactionId(transactionId);
        exception.setCustomerId(customerId);
        exception.setRuleName(ruleName);
        exception.setSeverity(severity);
        exception.setMessage(message);
        exception.setTimestamp(LocalDateTime.now());
        return repository.save(exception);
    }

    @Transactional(readOnly = true)
    public List<TaxException> search(String customerId, Severity severity, String ruleName) {
        Specification<TaxException> spec = (root, query, cb) -> cb.conjunction();

        if (customerId != null && !customerId.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("customerId"), customerId));
        }
        if (severity != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("severity"), severity));
        }
        if (ruleName != null && !ruleName.isBlank()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("ruleName"), ruleName));
        }
        return repository.findAll(spec, Sort.by(Sort.Direction.DESC, "timestamp"));
    }
}
