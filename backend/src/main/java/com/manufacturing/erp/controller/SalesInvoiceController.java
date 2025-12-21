package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Broker;
import com.manufacturing.erp.domain.Customer;
import com.manufacturing.erp.domain.SalesInvoice;
import com.manufacturing.erp.dto.SalesInvoiceDtos;
import com.manufacturing.erp.repository.BrokerRepository;
import com.manufacturing.erp.repository.CustomerRepository;
import com.manufacturing.erp.repository.SalesInvoiceRepository;
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
  private final CustomerRepository customerRepository;
  private final BrokerRepository brokerRepository;
  private final SalesInvoiceRepository salesInvoiceRepository;

  public SalesInvoiceController(SalesInvoiceService salesInvoiceService,
                                CustomerRepository customerRepository,
                                BrokerRepository brokerRepository,
                                SalesInvoiceRepository salesInvoiceRepository) {
    this.salesInvoiceService = salesInvoiceService;
    this.customerRepository = customerRepository;
    this.brokerRepository = brokerRepository;
    this.salesInvoiceRepository = salesInvoiceRepository;
  }

  @GetMapping
  public List<SalesInvoiceResponse> list() {
    return salesInvoiceRepository.findAll().stream()
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
  public SalesInvoice create(@Valid @RequestBody SalesInvoiceDtos.CreateSalesInvoiceRequest request) {
    Customer customer = customerRepository.findById(request.customerId())
        .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
    Broker broker = null;
    if (request.brokerId() != null) {
      broker = brokerRepository.findById(request.brokerId())
          .orElseThrow(() -> new IllegalArgumentException("Broker not found"));
    }
    SalesInvoice invoice = new SalesInvoice();
    invoice.setInvoiceNo(request.invoiceNo());
    invoice.setCustomer(customer);
    invoice.setBroker(broker);
    invoice.setInvoiceDate(request.invoiceDate());
    invoice.setTotalAmount(request.totalAmount());
    return salesInvoiceService.postInvoice(invoice);
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
