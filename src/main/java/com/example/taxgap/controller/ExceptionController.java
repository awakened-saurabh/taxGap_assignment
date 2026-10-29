package com.example.taxgap.controller;

import com.example.taxgap.domain.Severity;
import com.example.taxgap.domain.TaxException;
import com.example.taxgap.service.ExceptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/exceptions")
@RequiredArgsConstructor
public class ExceptionController {

    private final ExceptionService exceptionService;

    @GetMapping
    public List<TaxException> getExceptions(@RequestParam(required = false) String customerId,
                                            @RequestParam(required = false) Severity severity,
                                            @RequestParam(required = false) String ruleName) {
        return exceptionService.search(customerId, severity, ruleName);
    }
}
