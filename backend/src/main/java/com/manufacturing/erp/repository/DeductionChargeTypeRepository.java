package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.DeductionChargeType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeductionChargeTypeRepository extends JpaRepository<DeductionChargeType, Long> {
  boolean existsByCode(String code);
}
