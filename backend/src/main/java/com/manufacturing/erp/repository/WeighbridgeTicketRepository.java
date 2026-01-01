package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.WeighbridgeTicket;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeighbridgeTicketRepository extends JpaRepository<WeighbridgeTicket, Long> {
  List<WeighbridgeTicket> findByPurchaseOrderCompanyId(Long companyId);

  Optional<WeighbridgeTicket> findByIdAndPurchaseOrderCompanyId(Long id, Long companyId);
}
