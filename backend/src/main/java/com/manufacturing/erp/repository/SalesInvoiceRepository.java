package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.SalesInvoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesInvoiceRepository extends JpaRepository<SalesInvoice, Long> {}
