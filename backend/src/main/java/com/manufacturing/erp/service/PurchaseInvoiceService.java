package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.domain.Grn;
import com.manufacturing.erp.domain.GrnLine;
import com.manufacturing.erp.domain.PurchaseInvoice;
import com.manufacturing.erp.domain.PurchaseInvoiceLine;
import com.manufacturing.erp.dto.PurchaseInvoiceDtos;
import com.manufacturing.erp.repository.GrnRepository;
import com.manufacturing.erp.repository.PurchaseInvoiceLineRepository;
import com.manufacturing.erp.repository.PurchaseInvoiceRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseInvoiceService {
  private final PurchaseInvoiceRepository purchaseInvoiceRepository;
  private final PurchaseInvoiceLineRepository purchaseInvoiceLineRepository;
  private final GrnRepository grnRepository;

  public PurchaseInvoiceService(PurchaseInvoiceRepository purchaseInvoiceRepository,
                                PurchaseInvoiceLineRepository purchaseInvoiceLineRepository,
                                GrnRepository grnRepository) {
    this.purchaseInvoiceRepository = purchaseInvoiceRepository;
    this.purchaseInvoiceLineRepository = purchaseInvoiceLineRepository;
    this.grnRepository = grnRepository;
  }

  @Transactional
  public PurchaseInvoice createFromGrn(Long grnId) {
    Grn grn = grnRepository.findById(grnId)
        .orElseThrow(() -> new IllegalArgumentException("GRN not found"));
    if (grn.getStatus() != DocumentStatus.POSTED) {
      throw new IllegalStateException("GRN must be posted before creating purchase invoice");
    }
    var existing = purchaseInvoiceRepository.findFirstByGrnId(grnId);
    if (existing.isPresent()) {
      return existing.get();
    }

    PurchaseInvoice invoice = new PurchaseInvoice();
    invoice.setInvoiceNo(resolveInvoiceNo(null));
    invoice.setSupplier(grn.getSupplier());
    invoice.setPurchaseOrder(grn.getPurchaseOrder());
    invoice.setGrn(grn);
    invoice.setSupplierInvoiceNo(null);
    invoice.setInvoiceDate(LocalDate.now());
    invoice.setNarration(null);
    invoice.setStatus(DocumentStatus.DRAFT);

    BigDecimal subtotal = BigDecimal.ZERO;
    List<PurchaseInvoiceLine> lines = new ArrayList<>();
    for (GrnLine grnLine : grn.getLines()) {
      PurchaseInvoiceLine line = buildLine(invoice, grnLine);
      subtotal = subtotal.add(line.getAmount() != null ? line.getAmount() : BigDecimal.ZERO);
      lines.add(line);
    }

    invoice.setSubtotal(subtotal);
    invoice.setTaxTotal(BigDecimal.ZERO);
    invoice.setRoundOff(BigDecimal.ZERO);
    invoice.setGrandTotal(subtotal);
    invoice.setTotalAmount(subtotal);
    invoice.setTdsAmount(BigDecimal.ZERO);
    invoice.setNetPayable(subtotal);

    PurchaseInvoice saved = purchaseInvoiceRepository.save(invoice);
    List<PurchaseInvoiceLine> savedLines = lines.stream().map(line -> {
      line.setPurchaseInvoice(saved);
      return purchaseInvoiceLineRepository.save(line);
    }).toList();
    saved.setLines(savedLines);
    return saved;
  }

  @Transactional
  public PurchaseInvoice updateDraft(Long id, PurchaseInvoiceDtos.UpdateInvoiceRequest request) {
    PurchaseInvoice invoice = purchaseInvoiceRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Purchase invoice not found"));
    if (invoice.getStatus() != DocumentStatus.DRAFT) {
      throw new IllegalStateException("Only draft invoices can be edited");
    }
    invoice.setSupplierInvoiceNo(request.supplierInvoiceNo());
    invoice.setInvoiceDate(request.invoiceDate());
    invoice.setNarration(request.narration());
    return purchaseInvoiceRepository.save(invoice);
  }

  @Transactional
  public PurchaseInvoice post(Long id) {
    PurchaseInvoice invoice = purchaseInvoiceRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Purchase invoice not found"));
    if (invoice.getStatus() == DocumentStatus.POSTED) {
      return invoice;
    }
    invoice.setStatus(DocumentStatus.POSTED);
    return purchaseInvoiceRepository.save(invoice);
  }

  private PurchaseInvoiceLine buildLine(PurchaseInvoice invoice, GrnLine grnLine) {
    BigDecimal qty = resolveQty(grnLine);
    BigDecimal rate = grnLine.getRate() != null ? grnLine.getRate()
        : grnLine.getPurchaseOrderLine() != null ? grnLine.getPurchaseOrderLine().getRate() : BigDecimal.ZERO;
    BigDecimal amount = qty.multiply(rate != null ? rate : BigDecimal.ZERO);

    PurchaseInvoiceLine line = new PurchaseInvoiceLine();
    line.setPurchaseInvoice(invoice);
    line.setItem(grnLine.getItem());
    line.setUom(grnLine.getUom());
    line.setQuantity(qty);
    line.setRate(rate);
    line.setAmount(amount);
    return line;
  }

  private String resolveInvoiceNo(String provided) {
    if (provided != null && !provided.isBlank()) {
      return provided;
    }
    return "PINV-" + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + System.nanoTime();
  }

  private BigDecimal resolveQty(GrnLine line) {
    if (line.getAcceptedQty() != null) {
      return line.getAcceptedQty();
    }
    if (line.getReceivedQty() != null) {
      return line.getReceivedQty();
    }
    if (line.getQuantity() != null) {
      return line.getQuantity();
    }
    if (line.getExpectedQty() != null) {
      return line.getExpectedQty();
    }
    return BigDecimal.ZERO;
  }
}
