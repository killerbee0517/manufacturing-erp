package com.manufacturing.erp.controller;

import com.manufacturing.erp.dto.PurchaseInvoiceDtos;
import com.manufacturing.erp.service.PurchaseInvoiceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/purchase-invoices")
public class PurchaseInvoiceController {
  private final PurchaseInvoiceService purchaseInvoiceService;

  public PurchaseInvoiceController(PurchaseInvoiceService purchaseInvoiceService) {
    this.purchaseInvoiceService = purchaseInvoiceService;
  }

  @PostMapping
  public PurchaseInvoiceDtos.InvoiceResponse create(@Valid @RequestBody PurchaseInvoiceDtos.CreateInvoiceRequest request) {
    var invoice = purchaseInvoiceService.createInvoice(request);
    return new PurchaseInvoiceDtos.InvoiceResponse(invoice.getId(), invoice.getInvoiceNo(), invoice.getTotalAmount(),
        invoice.getTdsAmount(), invoice.getNetPayable());
  }
}
