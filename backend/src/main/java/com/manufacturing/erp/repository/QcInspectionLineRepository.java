package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.QcInspectionLine;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QcInspectionLineRepository extends JpaRepository<QcInspectionLine, Long> {
  List<QcInspectionLine> findByQcInspectionId(Long inspectionId);
}
