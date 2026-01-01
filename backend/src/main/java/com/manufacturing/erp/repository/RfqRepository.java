package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.Rfq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RfqRepository extends JpaRepository<Rfq, Long>, JpaSpecificationExecutor<Rfq> {
  java.util.Optional<Rfq> findByIdAndCompanyId(Long id, Long companyId);
}
