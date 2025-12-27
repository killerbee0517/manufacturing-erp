package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.ProcessTemplateStep;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessTemplateStepRepository extends JpaRepository<ProcessTemplateStep, Long> {
  List<ProcessTemplateStep> findByTemplateIdOrderByStepNoAsc(Long templateId);
}
