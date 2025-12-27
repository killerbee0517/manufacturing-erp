package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.ProductionBatchStep;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionBatchStepRepository extends JpaRepository<ProductionBatchStep, Long> {
  List<ProductionBatchStep> findByBatchIdOrderByStepNoAsc(Long batchId);
}
