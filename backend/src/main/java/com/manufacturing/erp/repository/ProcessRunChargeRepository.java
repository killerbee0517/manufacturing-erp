package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.ProcessRunCharge;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessRunChargeRepository extends JpaRepository<ProcessRunCharge, Long> {
  List<ProcessRunCharge> findByProcessRunId(Long processRunId);
}
