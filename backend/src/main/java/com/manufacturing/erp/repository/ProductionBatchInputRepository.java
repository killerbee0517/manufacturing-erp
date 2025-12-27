package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.ProductionBatchInput;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionBatchInputRepository extends JpaRepository<ProductionBatchInput, Long> {
  List<ProductionBatchInput> findByBatchId(Long batchId);
}
