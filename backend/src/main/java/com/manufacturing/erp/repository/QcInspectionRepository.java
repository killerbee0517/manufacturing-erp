package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.Enums.QcStatus;
import com.manufacturing.erp.domain.QcInspection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QcInspectionRepository extends JpaRepository<QcInspection, Long> {
  long countByStatus(QcStatus status);

  List<QcInspection> findByGrnId(Long grnId);

  boolean existsByGrnIdAndStatus(Long grnId, QcStatus status);
}
