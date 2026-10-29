package com.example.taxgap.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_txn", columnList = "transaction_id"),
        @Index(name = "idx_audit_event", columnList = "event_type")
})
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", length = 30, nullable = false)
    private EventType eventType;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "event_time")
    private LocalDateTime timestamp;

    @Column(name = "detail_json", columnDefinition = "TEXT")
    private String detailJson;
}
