package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.PurchaseInvoiceCharge;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseInvoiceChargeRepository extends JpaRepository<PurchaseInvoiceCharge, Long> {
  List<PurchaseInvoiceCharge> findByPurchaseInvoiceId(Long purchaseInvoiceId);
}
