package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.PurchaseInvoice;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseInvoiceRepository extends JpaRepository<PurchaseInvoice, Long> {
  @Query("select coalesce(sum(pi.totalAmount),0) from PurchaseInvoice pi where pi.supplier.id = :supplierId and pi.invoiceDate between :start and :end and pi.status = com.manufacturing.erp.domain.Enums.DocumentStatus.POSTED")
  java.math.BigDecimal findPostedTotalForSupplier(@Param("supplierId") Long supplierId,
      @Param("start") LocalDate start,
      @Param("end") LocalDate end);
}
