package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {}
