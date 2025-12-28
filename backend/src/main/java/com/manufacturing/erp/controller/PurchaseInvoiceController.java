package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.domain.PurchaseInvoice;
import com.manufacturing.erp.dto.PurchaseInvoiceDtos;
import com.manufacturing.erp.repository.PurchaseInvoiceLineRepository;
import com.manufacturing.erp.repository.PurchaseInvoiceRepository;
import com.manufacturing.erp.service.PurchaseInvoiceService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/purchase-invoices")
public class PurchaseInvoiceController {
  private final PurchaseInvoiceService purchaseInvoiceService;
  private final PurchaseInvoiceRepository purchaseInvoiceRepository;
  private final PurchaseInvoiceLineRepository purchaseInvoiceLineRepository;

  public PurchaseInvoiceController(PurchaseInvoiceService purchaseInvoiceService,
                                   PurchaseInvoiceRepository purchaseInvoiceRepository,
                                   PurchaseInvoiceLineRepository purchaseInvoiceLineRepository) {
    this.purchaseInvoiceService = purchaseInvoiceService;
    this.purchaseInvoiceRepository = purchaseInvoiceRepository;
    this.purchaseInvoiceLineRepository = purchaseInvoiceLineRepository;
  }

  @GetMapping
  public List<PurchaseInvoiceDtos.InvoiceResponse> list(@RequestParam(required = false) String status) {
    var invoices = purchaseInvoiceRepository.findAll();
    if (status != null && !status.isBlank()) {
      DocumentStatus filter = DocumentStatus.valueOf(status.toUpperCase());
      invoices = invoices.stream().filter(inv -> inv.getStatus() == filter).toList();
    }
    return invoices.stream().map(this::toResponse).toList();
  }

  @GetMapping("/{id}")
  public PurchaseInvoiceDtos.InvoiceResponse get(@PathVariable Long id) {
    var invoice = purchaseInvoiceRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Purchase invoice not found"));
    return toResponse(invoice);
  }

  @PostMapping("/from-grn/{grnId}")
  public PurchaseInvoiceDtos.InvoiceResponse createFromGrn(@PathVariable Long grnId) {
    var invoice = purchaseInvoiceService.createFromGrn(grnId);
    return toResponse(invoice);
  }

  @PutMapping("/{id}")
  public PurchaseInvoiceDtos.InvoiceResponse update(@PathVariable Long id,
                                                    @RequestBody @jakarta.validation.Valid PurchaseInvoiceDtos.UpdateInvoiceRequest request) {
    var invoice = purchaseInvoiceService.updateDraft(id, request);
    return toResponse(invoice);
  }

  @PostMapping("/{id}/post")
  public PurchaseInvoiceDtos.InvoiceResponse post(@PathVariable Long id) {
    var invoice = purchaseInvoiceService.post(id);
    return toResponse(invoice);
  }

  private PurchaseInvoiceDtos.InvoiceResponse toResponse(PurchaseInvoice invoice) {
    var lines = purchaseInvoiceLineRepository.findByPurchaseInvoiceId(invoice.getId());
    List<PurchaseInvoiceDtos.InvoiceLineResponse> lineResponses = lines.stream()
        .map(line -> new PurchaseInvoiceDtos.InvoiceLineResponse(
            line.getId(),
            line.getItem() != null ? line.getItem().getId() : null,
            line.getItem() != null ? line.getItem().getName() : null,
            line.getUom() != null ? line.getUom().getId() : null,
            line.getUom() != null ? line.getUom().getCode() : null,
            line.getQuantity(),
            line.getRate(),
            line.getAmount()))
        .toList();
    return new PurchaseInvoiceDtos.InvoiceResponse(
        invoice.getId(),
        invoice.getInvoiceNo(),
        invoice.getSupplier() != null ? invoice.getSupplier().getId() : null,
        invoice.getSupplier() != null ? invoice.getSupplier().getName() : null,
        invoice.getPurchaseOrder() != null ? invoice.getPurchaseOrder().getId() : null,
        invoice.getPurchaseOrder() != null ? invoice.getPurchaseOrder().getPoNo() : null,
        invoice.getGrn() != null ? invoice.getGrn().getId() : null,
        invoice.getGrn() != null ? invoice.getGrn().getGrnNo() : null,
        invoice.getSupplierInvoiceNo(),
        invoice.getInvoiceDate(),
        invoice.getNarration(),
        invoice.getStatus() != null ? invoice.getStatus().name() : null,
        invoice.getSubtotal(),
        invoice.getTaxTotal(),
        invoice.getRoundOff(),
        invoice.getGrandTotal(),
        lineResponses
    );
  }
}
