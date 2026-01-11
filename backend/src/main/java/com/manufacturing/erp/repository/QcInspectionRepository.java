package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.Enums.QcStatus;
import com.manufacturing.erp.domain.QcInspection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QcInspectionRepository extends JpaRepository<QcInspection, Long> {
  long countByStatus(QcStatus status);

  long countByStatusAndPurchaseOrderCompanyId(QcStatus status, Long companyId);

  List<QcInspection> findByGrnId(Long grnId);

  boolean existsByGrnIdAndStatus(Long grnId, QcStatus status);

  List<QcInspection> findByWeighbridgeTicketId(Long weighbridgeTicketId);

  List<QcInspection> findByPurchaseOrderId(Long purchaseOrderId);

  boolean existsByWeighbridgeTicketIdAndStatus(Long weighbridgeTicketId, QcStatus status);
}
