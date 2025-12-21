package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.PurchaseOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderLineRepository extends JpaRepository<PurchaseOrderLine, Long> {}
