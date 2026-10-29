package com.example.taxgap.service;

import com.example.taxgap.domain.TaxRule;
import com.example.taxgap.repository.TaxRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RuleService {

    private final TaxRuleRepository repository;

    public List<TaxRule> findAll() {
        return repository.findAll();
    }

    @Transactional
    public TaxRule toggle(Long id) {
        TaxRule rule = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rule " + id + " not found"));
        rule.setEnabled(!rule.isEnabled());
        return repository.save(rule);
    }
}
