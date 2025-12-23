package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.ProcessRunConsumption;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessRunConsumptionRepository extends JpaRepository<ProcessRunConsumption, Long> {
  List<ProcessRunConsumption> findByProcessRunId(Long processRunId);
}
