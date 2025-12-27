package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.ProcessTemplateInput;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessTemplateInputRepository extends JpaRepository<ProcessTemplateInput, Long> {
  List<ProcessTemplateInput> findByTemplateId(Long templateId);
}
