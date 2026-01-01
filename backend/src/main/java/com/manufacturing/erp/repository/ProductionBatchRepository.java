package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.ProductionBatch;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionBatchRepository extends JpaRepository<ProductionBatch, Long> {
  List<ProductionBatch> findByProductionOrderId(Long productionOrderId);

  List<ProductionBatch> findByCompanyId(Long companyId);

  Optional<ProductionBatch> findByIdAndCompanyId(Long id, Long companyId);
}
