package com.example.taxgap.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "tax_computations", indexes = {
        @Index(name = "idx_comp_status", columnList = "compliance_status")
})
@Getter
@Setter
@NoArgsConstructor
public class TaxComputation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_ref_id", nullable = false, unique = true)
    private FinancialTransaction transaction;

    @Column(name = "expected_tax", precision = 19, scale = 2)
    private BigDecimal expectedTax;

    @Column(name = "tax_gap", precision = 19, scale = 2)
    private BigDecimal taxGap;

    @Enumerated(EnumType.STRING)
    @Column(name = "compliance_status", length = 20, nullable = false)
    private ComplianceStatus complianceStatus;
}
