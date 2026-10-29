package com.example.taxgap.repository;

import com.example.taxgap.domain.TaxComputation;
import com.example.taxgap.dto.CustomerTaxSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaxComputationRepository extends JpaRepository<TaxComputation, Long> {

    @Query("select new com.example.taxgap.dto.CustomerTaxSummary("
            + "t.customerId, sum(t.amount), sum(t.reportedTax), sum(c.expectedTax), sum(c.taxGap), count(c), "
            + "sum(case when c.complianceStatus = com.example.taxgap.domain.ComplianceStatus.NON_COMPLIANT then 1L else 0L end)) "
            + "from TaxComputation c join c.transaction t "
            + "group by t.customerId order by t.customerId")
    List<CustomerTaxSummary> customerSummary();
}
