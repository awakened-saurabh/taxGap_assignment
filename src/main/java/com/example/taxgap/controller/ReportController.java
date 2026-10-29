package com.example.taxgap.controller;

import com.example.taxgap.dto.CustomerTaxSummary;
import com.example.taxgap.dto.ExceptionSummary;
import com.example.taxgap.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/customer-summary")
    public List<CustomerTaxSummary> customerSummary() {
        return reportService.customerSummary();
    }

    @GetMapping("/exception-summary")
    public ExceptionSummary exceptionSummary() {
        return reportService.exceptionSummary();
    }
}
