package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.ProcessTemplateOutput;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessTemplateOutputRepository extends JpaRepository<ProcessTemplateOutput, Long> {
  List<ProcessTemplateOutput> findByTemplateId(Long templateId);
}
