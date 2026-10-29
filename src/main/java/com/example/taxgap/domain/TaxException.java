package com.example.taxgap.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tax_exceptions", indexes = {
        @Index(name = "idx_exc_customer", columnList = "customer_id"),
        @Index(name = "idx_exc_severity", columnList = "severity"),
        @Index(name = "idx_exc_rule", columnList = "rule_name")
})
@Getter
@Setter
@NoArgsConstructor
public class TaxException {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "customer_id", length = 100)
    private String customerId;

    @Column(name = "rule_name", length = 100)
    private String ruleName;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private Severity severity;

    @Column(length = 500)
    private String message;

    @Column(name = "event_time")
    private LocalDateTime timestamp;
}
