package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.ProcessTemplateStepCharge;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessTemplateStepChargeRepository extends JpaRepository<ProcessTemplateStepCharge, Long> {
  List<ProcessTemplateStepCharge> findByStepId(Long stepId);
}
