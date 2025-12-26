package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.StockTransferLine;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockTransferLineRepository extends JpaRepository<StockTransferLine, Long> {
  List<StockTransferLine> findByHeaderId(Long headerId);
}
