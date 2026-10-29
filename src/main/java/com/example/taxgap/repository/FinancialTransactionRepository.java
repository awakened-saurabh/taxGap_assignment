package com.example.taxgap.repository;

import com.example.taxgap.domain.FinancialTransaction;
import com.example.taxgap.domain.TransactionType;
import com.example.taxgap.domain.ValidationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, Long> {

    boolean existsByTransactionIdAndValidationStatus(String transactionId, ValidationStatus status);

    Optional<FinancialTransaction> findFirstByTransactionIdAndValidationStatus(String transactionId, ValidationStatus status);

    @Query("select coalesce(sum(t.amount), 0) from FinancialTransaction t "
            + "where t.customerId = :customerId and t.transactionType = :type and t.validationStatus = :status")
    BigDecimal sumAmount(@Param("customerId") String customerId,
                         @Param("type") TransactionType type,
                         @Param("status") ValidationStatus status);
}
