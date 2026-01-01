package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.ProcessTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProcessTemplateRepository extends JpaRepository<ProcessTemplate, Long> {
  List<ProcessTemplate> findByCompanyId(Long companyId);

  Optional<ProcessTemplate> findByIdAndCompanyId(Long id, Long companyId);
}
