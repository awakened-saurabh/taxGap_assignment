package com.example.taxgap.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tax_rules")
@Getter
@Setter
@NoArgsConstructor
public class TaxRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_name", nullable = false, unique = true, length = 100)
    private String ruleName;

    @Column(name = "rule_type", nullable = false, length = 50)
    private String ruleType;

    @Column(name = "config_json", columnDefinition = "TEXT")
    private String configJson;

    @Column(nullable = false)
    private boolean enabled = true;

    public TaxRule(String ruleName, String ruleType, String configJson, boolean enabled) {
        this.ruleName = ruleName;
        this.ruleType = ruleType;
        this.configJson = configJson;
        this.enabled = enabled;
    }
}
