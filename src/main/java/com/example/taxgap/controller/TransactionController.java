package com.example.taxgap.controller;

import com.example.taxgap.dto.TransactionRequest;
import com.example.taxgap.dto.UploadResponse;
import com.example.taxgap.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/upload")
    public UploadResponse upload(@RequestBody List<TransactionRequest> requests) {
        return transactionService.upload(requests);
    }
}
