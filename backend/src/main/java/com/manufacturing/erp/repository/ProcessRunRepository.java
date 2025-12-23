package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.ProcessRun;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessRunRepository extends JpaRepository<ProcessRun, Long> {
  List<ProcessRun> findByProductionBatchId(Long productionBatchId);
}
