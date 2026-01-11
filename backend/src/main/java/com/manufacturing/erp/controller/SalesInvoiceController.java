package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.SalesInvoice;
import com.manufacturing.erp.dto.StockDtos;
import com.manufacturing.erp.repository.SalesInvoiceRepository;
import com.manufacturing.erp.security.CompanyContext;
import com.manufacturing.erp.service.SalesInvoiceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sales-invoices")
public class SalesInvoiceController {
  private final SalesInvoiceService salesInvoiceService;
  private final SalesInvoiceRepository salesInvoiceRepository;
  private final CompanyContext companyContext;

  public SalesInvoiceController(SalesInvoiceService salesInvoiceService,
                                SalesInvoiceRepository salesInvoiceRepository,
                                CompanyContext companyContext) {
    this.salesInvoiceService = salesInvoiceService;
    this.salesInvoiceRepository = salesInvoiceRepository;
    this.companyContext = companyContext;
  }

  @GetMapping
  public List<SalesInvoiceResponse> list() {
    Long companyId = requireCompanyId();
    return salesInvoiceRepository.findByCompanyId(companyId).stream()
        .map(invoice -> new SalesInvoiceResponse(
            invoice.getId(),
            invoice.getInvoiceNo(),
            invoice.getCustomer() != null ? invoice.getCustomer().getId() : null,
            invoice.getBroker() != null ? invoice.getBroker().getId() : null,
            invoice.getInvoiceDate(),
            invoice.getTotalAmount(),
            invoice.getStatus().name()))
        .toList();
  }

  @PostMapping
  public SalesInvoice create(@Valid @RequestBody StockDtos.SalesInvoiceRequest request) {
    return salesInvoiceService.postInvoice(request);
  }

  private Long requireCompanyId() {
    Long companyId = companyContext.getCompanyId();
    if (companyId == null) {
      throw new IllegalArgumentException("Missing company context");
    }
    return companyId;
  }

  public record SalesInvoiceResponse(
      Long id,
      String invoiceNo,
      Long customerId,
      Long brokerId,
      java.time.LocalDate invoiceDate,
      java.math.BigDecimal totalAmount,
      String status) {}
}
