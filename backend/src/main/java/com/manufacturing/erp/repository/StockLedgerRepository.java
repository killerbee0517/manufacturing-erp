package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.StockLedger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockLedgerRepository extends JpaRepository<StockLedger, Long> {}
