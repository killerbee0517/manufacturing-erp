package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.ProductionOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, Long> {
}
