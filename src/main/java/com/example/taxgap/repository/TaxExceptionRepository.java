package com.example.taxgap.repository;

import com.example.taxgap.domain.TaxException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaxExceptionRepository extends JpaRepository<TaxException, Long>, JpaSpecificationExecutor<TaxException> {

    @Query("select e.severity, count(e) from TaxException e group by e.severity")
    List<Object[]> severityCounts();

    @Query("select e.customerId, count(e) from TaxException e group by e.customerId order by e.customerId")
    List<Object[]> customerCounts();
}
