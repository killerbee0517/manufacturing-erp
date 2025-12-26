package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.ProcessRunOutput;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessRunOutputRepository extends JpaRepository<ProcessRunOutput, Long> {
  List<ProcessRunOutput> findByProcessRunId(Long processRunId);

  List<ProcessRunOutput> findByProcessRunProductionBatchId(Long batchId);
}
