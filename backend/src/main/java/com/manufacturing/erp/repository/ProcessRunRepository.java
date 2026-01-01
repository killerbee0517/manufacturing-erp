package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.ProcessRun;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessRunRepository extends JpaRepository<ProcessRun, Long> {
  List<ProcessRun> findByProductionBatchId(Long productionBatchId);

  List<ProcessRun> findByProductionBatchIdAndCompanyId(Long productionBatchId, Long companyId);

  Optional<ProcessRun> findByIdAndCompanyId(Long id, Long companyId);
}
