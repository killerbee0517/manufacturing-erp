package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.domain.Item;
import com.manufacturing.erp.domain.PurchaseInvoice;
import com.manufacturing.erp.domain.PurchaseInvoiceLine;
import com.manufacturing.erp.domain.Supplier;
import com.manufacturing.erp.domain.TdsDeduction;
import com.manufacturing.erp.dto.PurchaseInvoiceDtos;
import com.manufacturing.erp.repository.ItemRepository;
import com.manufacturing.erp.repository.PurchaseInvoiceLineRepository;
import com.manufacturing.erp.repository.PurchaseInvoiceRepository;
import com.manufacturing.erp.repository.SupplierRepository;
import com.manufacturing.erp.repository.TdsDeductionRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseInvoiceService {
  private final PurchaseInvoiceRepository purchaseInvoiceRepository;
  private final SupplierRepository supplierRepository;
  private final ItemRepository itemRepository;
  private final PurchaseInvoiceLineRepository purchaseInvoiceLineRepository;
  private final TdsDeductionRepository tdsDeductionRepository;
  private final TdsService tdsService;

  public PurchaseInvoiceService(PurchaseInvoiceRepository purchaseInvoiceRepository,
                                SupplierRepository supplierRepository,
                                ItemRepository itemRepository,
                                PurchaseInvoiceLineRepository purchaseInvoiceLineRepository,
                                TdsDeductionRepository tdsDeductionRepository,
                                TdsService tdsService) {
    this.purchaseInvoiceRepository = purchaseInvoiceRepository;
    this.supplierRepository = supplierRepository;
    this.itemRepository = itemRepository;
    this.purchaseInvoiceLineRepository = purchaseInvoiceLineRepository;
    this.tdsDeductionRepository = tdsDeductionRepository;
    this.tdsService = tdsService;
  }

  @Transactional
  public PurchaseInvoice createInvoice(PurchaseInvoiceDtos.CreateInvoiceRequest request) {
    Supplier supplier = supplierRepository.findById(request.supplierId())
        .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));

    BigDecimal total = request.lines().stream()
        .map(PurchaseInvoiceDtos.LineRequest::lineAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal tdsAmount = tdsService.calculateTds(supplier.getId(), request.invoiceDate(), total);
    BigDecimal netPayable = total.subtract(tdsAmount);

    PurchaseInvoice invoice = new PurchaseInvoice();
    invoice.setInvoiceNo(request.invoiceNo());
    invoice.setSupplier(supplier);
    invoice.setInvoiceDate(request.invoiceDate());
    invoice.setTotalAmount(total);
    invoice.setTdsAmount(tdsAmount);
    invoice.setNetPayable(netPayable);
    invoice.setStatus(DocumentStatus.POSTED);
    PurchaseInvoice saved = purchaseInvoiceRepository.save(invoice);

    List<PurchaseInvoiceLine> lines = request.lines().stream().map(line -> {
      Item item = itemRepository.findById(line.itemId())
          .orElseThrow(() -> new IllegalArgumentException("Item not found"));
      PurchaseInvoiceLine entity = new PurchaseInvoiceLine();
      entity.setPurchaseInvoice(saved);
      entity.setItem(item);
      entity.setQuantity(line.quantity());
      entity.setLineAmount(line.lineAmount());
      return purchaseInvoiceLineRepository.save(entity);
    }).toList();

    if (tdsAmount.compareTo(BigDecimal.ZERO) > 0) {
      TdsDeduction deduction = new TdsDeduction();
      deduction.setPurchaseInvoice(saved);
      deduction.setSectionCode("AUTO");
      deduction.setTdsAmount(tdsAmount);
      tdsDeductionRepository.save(deduction);
    }

    return saved;
  }
}
