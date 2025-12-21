package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.Enums.QcStatus;
import com.manufacturing.erp.domain.QcInspection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QcInspectionRepository extends JpaRepository<QcInspection, Long> {
  long countByStatus(QcStatus status);
}
