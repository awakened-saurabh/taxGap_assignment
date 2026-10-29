package com.example.taxgap.config;

import com.example.taxgap.domain.AppUser;
import com.example.taxgap.domain.TaxRule;
import com.example.taxgap.repository.TaxRuleRepository;
import com.example.taxgap.repository.UserRepository;
import com.example.taxgap.rules.GstSlabRule;
import com.example.taxgap.rules.HighValueRule;
import com.example.taxgap.rules.RefundValidationRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TaxRuleRepository ruleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            userRepository.saveAll(List.of(
                    new AppUser("admin", passwordEncoder.encode("admin123"), "ADMIN"),
                    new AppUser("auditor", passwordEncoder.encode("auditor123"), "AUDITOR")));
            log.info("Seeded default users");
        }

        if (ruleRepository.count() == 0) {
            ruleRepository.saveAll(List.of(
                    new TaxRule("High Value Transaction Rule", HighValueRule.TYPE,
                            "{\"threshold\": 1000000, \"severity\": \"HIGH\"}", true),
                    new TaxRule("Refund Validation Rule", RefundValidationRule.TYPE,
                            "{\"severity\": \"HIGH\"}", true),
                    new TaxRule("GST Slab Violation Rule", GstSlabRule.TYPE,
                            "{\"slabs\": [{\"above\": 50000, \"minRate\": 0.12}, {\"above\": 100000, \"minRate\": 0.18}], \"severity\": \"MEDIUM\"}",
                            true)));
            log.info("Seeded default tax rules");
        }
    }
}
