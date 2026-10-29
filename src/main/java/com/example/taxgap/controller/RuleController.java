package com.example.taxgap.controller;

import com.example.taxgap.domain.TaxRule;
import com.example.taxgap.service.RuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rules")
@RequiredArgsConstructor
public class RuleController {

    private final RuleService ruleService;

    @GetMapping
    public List<TaxRule> getRules() {
        return ruleService.findAll();
    }

    @PutMapping("/{id}/toggle")
    public TaxRule toggle(@PathVariable Long id) {
        return ruleService.toggle(id);
    }
}
