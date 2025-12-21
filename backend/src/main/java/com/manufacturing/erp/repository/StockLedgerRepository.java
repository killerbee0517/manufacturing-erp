package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.StockLedger;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockLedgerRepository extends JpaRepository<StockLedger, Long> {
  List<StockLedger> findByDocType(String docType);
}
