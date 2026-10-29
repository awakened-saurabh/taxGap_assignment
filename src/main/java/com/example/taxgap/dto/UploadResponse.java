package com.example.taxgap.dto;

import java.util.List;

public record UploadResponse(int total, long success, long failure, List<TransactionResult> results) {
}
