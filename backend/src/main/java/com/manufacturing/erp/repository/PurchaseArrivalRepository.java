package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.PurchaseArrival;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseArrivalRepository extends JpaRepository<PurchaseArrival, Long> {
  Optional<PurchaseArrival> findFirstByWeighbridgeTicketIdOrderByCreatedAtDesc(Long weighbridgeTicketId);

  Optional<PurchaseArrival> findFirstByPurchaseOrderIdOrderByCreatedAtDesc(Long purchaseOrderId);

  List<PurchaseArrival> findAllByPurchaseOrderCompanyId(Long companyId);

  Optional<PurchaseArrival> findByIdAndPurchaseOrderCompanyId(Long id, Long companyId);
}
