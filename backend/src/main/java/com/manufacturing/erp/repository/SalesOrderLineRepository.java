package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.SalesOrderLine;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesOrderLineRepository extends JpaRepository<SalesOrderLine, Long> {
  List<SalesOrderLine> findBySalesOrderId(Long salesOrderId);
}
