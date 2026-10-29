package com.example.taxgap.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_txn_customer", columnList = "customer_id"),
        @Index(name = "idx_txn_txn_id", columnList = "transaction_id")
})
@Getter
@Setter
@NoArgsConstructor
public class FinancialTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "txn_date")
    private LocalDate date;

    @Column(name = "customer_id", length = 100)
    private String customerId;

    @Column(precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "tax_rate", precision = 9, scale = 4)
    private BigDecimal taxRate;

    @Column(name = "reported_tax", precision = 19, scale = 2)
    private BigDecimal reportedTax;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 20)
    private TransactionType transactionType;

    @Column(name = "original_transaction_id", length = 100)
    private String originalTransactionId;

    @Column(name = "raw_payload", columnDefinition = "TEXT")
    private String rawPayload;

    @Enumerated(EnumType.STRING)
    @Column(name = "validation_status", length = 20, nullable = false)
    private ValidationStatus validationStatus;

    @Column(name = "failure_reasons", length = 1000)
    private String failureReasons;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
