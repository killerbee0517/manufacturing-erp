package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.ProductionOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionOrderRepository extends JpaRepository<ProductionOrder, Long> {
  List<ProductionOrder> findByCompanyId(Long companyId);

  Optional<ProductionOrder> findByIdAndCompanyId(Long id, Long companyId);
}
