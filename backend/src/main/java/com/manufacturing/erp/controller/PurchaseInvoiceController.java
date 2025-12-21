package com.manufacturing.erp.controller;

import com.manufacturing.erp.dto.PurchaseInvoiceDtos;
import com.manufacturing.erp.repository.PurchaseInvoiceRepository;
import com.manufacturing.erp.service.PurchaseInvoiceService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/purchase-invoices")
public class PurchaseInvoiceController {
  private final PurchaseInvoiceService purchaseInvoiceService;
  private final PurchaseInvoiceRepository purchaseInvoiceRepository;

  public PurchaseInvoiceController(PurchaseInvoiceService purchaseInvoiceService,
                                   PurchaseInvoiceRepository purchaseInvoiceRepository) {
    this.purchaseInvoiceService = purchaseInvoiceService;
    this.purchaseInvoiceRepository = purchaseInvoiceRepository;
  }

  @GetMapping
  public List<PurchaseInvoiceDtos.InvoiceResponse> list() {
    return purchaseInvoiceRepository.findAll().stream()
        .map(invoice -> new PurchaseInvoiceDtos.InvoiceResponse(
            invoice.getId(),
            invoice.getInvoiceNo(),
            invoice.getTotalAmount(),
            invoice.getTdsAmount(),
            invoice.getNetPayable()))
        .toList();
  }

  @PostMapping
  public PurchaseInvoiceDtos.InvoiceResponse create(@Valid @RequestBody PurchaseInvoiceDtos.CreateInvoiceRequest request) {
    var invoice = purchaseInvoiceService.createInvoice(request);
    return new PurchaseInvoiceDtos.InvoiceResponse(invoice.getId(), invoice.getInvoiceNo(), invoice.getTotalAmount(),
        invoice.getTdsAmount(), invoice.getNetPayable());
  }
}
