package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Company;
import com.manufacturing.erp.domain.DebitNote;
import com.manufacturing.erp.domain.DebitNoteLine;
import com.manufacturing.erp.domain.Enums.CalcType;
import com.manufacturing.erp.domain.Enums.DebitNoteReason;
import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.domain.Enums.LedgerType;
import com.manufacturing.erp.domain.Ledger;
import com.manufacturing.erp.domain.PurchaseInvoice;
import com.manufacturing.erp.domain.PurchaseInvoiceCharge;
import com.manufacturing.erp.domain.Supplier;
import com.manufacturing.erp.dto.DebitNoteDtos;
import com.manufacturing.erp.repository.CompanyRepository;
import com.manufacturing.erp.repository.DebitNoteLineRepository;
import com.manufacturing.erp.repository.DebitNoteRepository;
import com.manufacturing.erp.repository.PurchaseInvoiceChargeRepository;
import com.manufacturing.erp.repository.PurchaseInvoiceRepository;
import com.manufacturing.erp.repository.SupplierRepository;
import com.manufacturing.erp.security.CompanyContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DebitNoteService {
  private final DebitNoteRepository debitNoteRepository;
  private final DebitNoteLineRepository debitNoteLineRepository;
  private final PurchaseInvoiceRepository purchaseInvoiceRepository;
  private final PurchaseInvoiceChargeRepository purchaseInvoiceChargeRepository;
  private final LedgerService ledgerService;
  private final VoucherService voucherService;
  private final SupplierRepository supplierRepository;
  private final CompanyRepository companyRepository;
  private final CompanyContext companyContext;

  public DebitNoteService(DebitNoteRepository debitNoteRepository,
                          DebitNoteLineRepository debitNoteLineRepository,
                          PurchaseInvoiceRepository purchaseInvoiceRepository,
                          PurchaseInvoiceChargeRepository purchaseInvoiceChargeRepository,
                          LedgerService ledgerService,
                          VoucherService voucherService,
                          SupplierRepository supplierRepository,
                          CompanyRepository companyRepository,
                          CompanyContext companyContext) {
    this.debitNoteRepository = debitNoteRepository;
    this.debitNoteLineRepository = debitNoteLineRepository;
    this.purchaseInvoiceRepository = purchaseInvoiceRepository;
    this.purchaseInvoiceChargeRepository = purchaseInvoiceChargeRepository;
    this.ledgerService = ledgerService;
    this.voucherService = voucherService;
    this.supplierRepository = supplierRepository;
    this.companyRepository = companyRepository;
    this.companyContext = companyContext;
  }

  @Transactional
  public DebitNote createFromInvoice(Long invoiceId) {
    PurchaseInvoice invoice = getInvoice(invoiceId);
    var existing = debitNoteRepository.findFirstByPurchaseInvoiceId(invoiceId);
    if (existing.isPresent()) {
      return existing.get();
    }
    DebitNote note = new DebitNote();
    note.setDebitNoteNo(resolveDnNo(null));
    note.setSupplier(invoice.getSupplier());
    note.setPurchaseInvoice(invoice);
    note.setPurchaseOrder(invoice.getPurchaseOrder());
    note.setGrn(invoice.getGrn());
    note.setDnDate(LocalDate.now());
    note.setNarration(null);
    note.setStatus(DocumentStatus.DRAFT);
    note.setReason(DebitNoteReason.WEIGHT_DIFF);
    note.setLines(new ArrayList<>());

    DebitNote saved = debitNoteRepository.save(note);
    List<DebitNoteLine> lines = new ArrayList<>();
    BigDecimal total = BigDecimal.ZERO;
    for (PurchaseInvoiceCharge charge : purchaseInvoiceChargeRepository.findByPurchaseInvoiceId(invoice.getId())) {
      if (!charge.isDeduction()) {
        continue;
      }
      DebitNoteLine line = new DebitNoteLine();
      line.setDebitNote(saved);
      line.setDescription(charge.getChargeType() != null ? charge.getChargeType().getName() : "Invoice deduction");
      line.setBaseValue(charge.getCalcType() == CalcType.PERCENT ? invoice.getSubtotal() : null);
      line.setRate(charge.getRate());
      line.setAmount(charge.getAmount());
      line.setRemarks(null);
      lines.add(line);
      total = total.add(charge.getAmount() != null ? charge.getAmount() : BigDecimal.ZERO);
    }
    if (!lines.isEmpty()) {
      debitNoteLineRepository.saveAll(lines);
      saved.setLines(lines);
    }
    saved.setTotalDeduction(total);
    return debitNoteRepository.save(saved);
  }

  @Transactional
  public DebitNote updateDraft(Long id, DebitNoteDtos.UpdateDebitNoteRequest request) {
    DebitNote note = getNote(id);
    if (note.getStatus() != DocumentStatus.DRAFT) {
      throw new IllegalStateException("Only draft debit notes can be edited");
    }
    note.setDnDate(request.dnDate() != null ? request.dnDate() : LocalDate.now());
    note.setNarration(request.narration());

    note.getLines().clear();

    BigDecimal total = BigDecimal.ZERO;
    List<DebitNoteLine> lines = new ArrayList<>();
    if (request.lines() != null) {
      for (DebitNoteDtos.LineRequest lineRequest : request.lines()) {
        DebitNoteLine line = new DebitNoteLine();
        line.setDebitNote(note);
        line.setRuleId(lineRequest.ruleId());
        line.setDescription(lineRequest.description());
        line.setBaseValue(lineRequest.baseValue());
        line.setRate(lineRequest.rate());
        BigDecimal amount = lineRequest.amount();
        if (amount == null) {
          BigDecimal base = lineRequest.baseValue() != null ? lineRequest.baseValue() : BigDecimal.ZERO;
          BigDecimal rate = lineRequest.rate() != null ? lineRequest.rate() : BigDecimal.ZERO;
          amount = base.multiply(rate).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        }
        line.setAmount(amount);
        line.setRemarks(lineRequest.remarks());
        total = total.add(amount != null ? amount : BigDecimal.ZERO);
        lines.add(line);
      }
      debitNoteLineRepository.saveAll(lines);
    }
    note.setTotalDeduction(total);
    note.getLines().addAll(lines);
    return debitNoteRepository.save(note);
  }

  @Transactional
  public DebitNote post(Long id) {
    DebitNote note = getNote(id);
    if (note.getStatus() == DocumentStatus.POSTED) {
      return note;
    }
    BigDecimal total = debitNoteLineRepository.findByDebitNoteId(note.getId()).stream()
        .map(line -> line.getAmount() != null ? line.getAmount() : BigDecimal.ZERO)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    note.setTotalDeduction(total);
    if (total.compareTo(BigDecimal.ZERO) > 0) {
      Ledger supplierLedger = ensureSupplierLedger(note.getSupplier());
      Ledger deductionLedger = ledgerService.findOrCreateLedger("Purchase Deductions", LedgerType.GENERAL);
      if (supplierLedger != null) {
        List<VoucherService.VoucherLineRequest> lines = new ArrayList<>();
        lines.add(new VoucherService.VoucherLineRequest(supplierLedger, total, BigDecimal.ZERO));
        lines.add(new VoucherService.VoucherLineRequest(deductionLedger, BigDecimal.ZERO, total));
        voucherService.createVoucher("DEBIT_NOTE", note.getId(),
            note.getDnDate() != null ? note.getDnDate() : LocalDate.now(),
            "Debit note posting", lines);
      }
    }
    note.setStatus(DocumentStatus.POSTED);
    return debitNoteRepository.save(note);
  }

  private Ledger ensureSupplierLedger(Supplier supplier) {
    if (supplier == null) {
      return null;
    }
    Ledger ledger = supplier.getLedger();
    if (ledger == null) {
      ledger = ledgerService.createLedger(supplier.getName(), LedgerType.SUPPLIER, "SUPPLIER", supplier.getId());
      supplier.setLedger(ledger);
      supplierRepository.save(supplier);
    }
    return ledger;
  }

  private PurchaseInvoice getInvoice(Long invoiceId) {
    Company company = requireCompany();
    return purchaseInvoiceRepository.findByIdAndPurchaseOrderCompanyId(invoiceId, company.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase invoice not found"));
  }

  private DebitNote getNote(Long noteId) {
    Company company = requireCompany();
    return debitNoteRepository.findByIdAndPurchaseOrderCompanyId(noteId, company.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Debit note not found"));
  }

  private Company requireCompany() {
    Long companyId = companyContext.getCompanyId();
    if (companyId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing company context");
    }
    return companyRepository.findById(companyId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
  }

  private String resolveDnNo(String provided) {
    if (provided != null && !provided.isBlank()) {
      return provided;
    }
    return "DN-" + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + System.nanoTime();
  }
}
