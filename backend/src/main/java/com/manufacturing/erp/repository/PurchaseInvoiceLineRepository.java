package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.PurchaseInvoiceLine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PurchaseInvoiceLineRepository extends JpaRepository<PurchaseInvoiceLine, Long> {
  List<PurchaseInvoiceLine> findByPurchaseInvoiceId(Long purchaseInvoiceId);
}
