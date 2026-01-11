package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.domain.Grn;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface GrnRepository extends JpaRepository<Grn, Long> {
  long countByStatusNot(DocumentStatus status);

  long countByStatusNotAndPurchaseOrderCompanyId(DocumentStatus status, Long companyId);

  Optional<Grn> findFirstByWeighbridgeTicketId(Long weighbridgeTicketId);

  Optional<Grn> findFirstByWeighbridgeTicketIdAndPurchaseOrderCompanyId(Long weighbridgeTicketId, Long companyId);

  List<Grn> findByPurchaseOrderCompanyId(Long companyId);

  Optional<Grn> findByIdAndPurchaseOrderCompanyId(Long id, Long companyId);
}
