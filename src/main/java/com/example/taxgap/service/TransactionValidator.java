package com.example.taxgap.service;

import com.example.taxgap.domain.TransactionType;
import com.example.taxgap.dto.TransactionRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Component
public class TransactionValidator {

    public List<String> validate(TransactionRequest request) {
        List<String> errors = new ArrayList<>();

        if (isBlank(request.transactionId())) {
            errors.add("transactionId is required");
        }
        if (isBlank(request.customerId())) {
            errors.add("customerId is required");
        }

        if (isBlank(request.date())) {
            errors.add("date is required");
        } else {
            try {
                LocalDate.parse(request.date().trim());
            } catch (DateTimeParseException e) {
                errors.add("date must be a valid date in yyyy-MM-dd format");
            }
        }

        if (request.amount() == null) {
            errors.add("amount is required");
        } else if (request.amount().signum() <= 0) {
            errors.add("amount must be greater than 0");
        }

        if (request.taxRate() == null) {
            errors.add("taxRate is required");
        } else if (request.taxRate().signum() < 0) {
            errors.add("taxRate must not be negative");
        }

        if (request.reportedTax() != null && request.reportedTax().signum() < 0) {
            errors.add("reportedTax must not be negative");
        }

        if (isBlank(request.transactionType())) {
            errors.add("transactionType is required");
        } else if (!isValidType(request.transactionType())) {
            errors.add("transactionType must be one of SALE, REFUND, EXPENSE");
        }

        return errors;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private boolean isValidType(String value) {
        try {
            TransactionType.valueOf(value.trim().toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
