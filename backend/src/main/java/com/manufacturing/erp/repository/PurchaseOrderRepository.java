package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.PurchaseOrder;
import com.manufacturing.erp.domain.Enums.DocumentStatus;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long>, JpaSpecificationExecutor<PurchaseOrder> {
  List<PurchaseOrder> findByRfqId(Long rfqId);

  List<PurchaseOrder> findByRfqIdAndCompanyId(Long rfqId, Long companyId);

  List<PurchaseOrder> findByCompanyId(Long companyId);

  java.util.Optional<PurchaseOrder> findByIdAndCompanyId(Long id, Long companyId);

  long countByCompanyIdAndPoDate(Long companyId, LocalDate poDate);

  long countByCompanyIdAndStatus(Long companyId, DocumentStatus status);

  long countByCompanyIdAndPoDateBetween(Long companyId, LocalDate startDate, LocalDate endDate);
}
