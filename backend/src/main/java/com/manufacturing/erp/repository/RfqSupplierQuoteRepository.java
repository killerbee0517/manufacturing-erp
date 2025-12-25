package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.RfqSupplierQuote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RfqSupplierQuoteRepository extends JpaRepository<RfqSupplierQuote, Long> {}
