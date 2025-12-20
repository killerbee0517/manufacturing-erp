package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.PurchaseInvoiceLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseInvoiceLineRepository extends JpaRepository<PurchaseInvoiceLine, Long> {}
