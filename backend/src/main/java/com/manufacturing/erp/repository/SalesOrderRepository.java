package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.SalesOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
  List<SalesOrder> findByCompanyId(Long companyId);

  Optional<SalesOrder> findByIdAndCompanyId(Long id, Long companyId);
}
