package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {}
