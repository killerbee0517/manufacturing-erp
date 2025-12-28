package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.domain.PurchaseInvoice;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseInvoiceRepository extends JpaRepository<PurchaseInvoice, Long> {
  @Query("select coalesce(sum(pi.totalAmount),0) from PurchaseInvoice pi where pi.supplier.id = :supplierId and pi.invoiceDate between :start and :end and pi.status = :status")
  java.math.BigDecimal findPostedTotalForSupplier(@Param("supplierId") Long supplierId,
      @Param("start") LocalDate start,
      @Param("end") LocalDate end,
      @Param("status") DocumentStatus status);

  Optional<PurchaseInvoice> findFirstByGrnId(Long grnId);
}
