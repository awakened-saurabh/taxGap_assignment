package com.example.taxgap.repository;

import com.example.taxgap.domain.TaxRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaxRuleRepository extends JpaRepository<TaxRule, Long> {

    List<TaxRule> findByEnabledTrue();
}
