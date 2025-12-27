package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.ProductionBatchOutput;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionBatchOutputRepository extends JpaRepository<ProductionBatchOutput, Long> {
  List<ProductionBatchOutput> findByBatchId(Long batchId);
}
