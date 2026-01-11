package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.SalesInvoice;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, Long> {
  List<SalesInvoice> findByCompanyId(Long companyId);

  Optional<SalesInvoice> findByIdAndCompanyId(Long id, Long companyId);

  long countByCompanyIdAndInvoiceDate(Long companyId, LocalDate invoiceDate);

  long countByCompanyIdAndInvoiceDateBetween(Long companyId, LocalDate startDate, LocalDate endDate);
}
