package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.DebitNote;
import com.manufacturing.erp.domain.DebitNoteLine;
import com.manufacturing.erp.domain.Enums.DebitNoteReason;
import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.domain.PurchaseInvoice;
import com.manufacturing.erp.dto.DebitNoteDtos;
import com.manufacturing.erp.repository.DebitNoteLineRepository;
import com.manufacturing.erp.repository.DebitNoteRepository;
import com.manufacturing.erp.repository.PurchaseInvoiceRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DebitNoteService {
  private final DebitNoteRepository debitNoteRepository;
  private final DebitNoteLineRepository debitNoteLineRepository;
  private final PurchaseInvoiceRepository purchaseInvoiceRepository;

  public DebitNoteService(DebitNoteRepository debitNoteRepository,
                          DebitNoteLineRepository debitNoteLineRepository,
                          PurchaseInvoiceRepository purchaseInvoiceRepository) {
    this.debitNoteRepository = debitNoteRepository;
    this.debitNoteLineRepository = debitNoteLineRepository;
    this.purchaseInvoiceRepository = purchaseInvoiceRepository;
  }

  @Transactional
  public DebitNote createFromInvoice(Long invoiceId) {
    PurchaseInvoice invoice = purchaseInvoiceRepository.findById(invoiceId)
        .orElseThrow(() -> new IllegalArgumentException("Purchase invoice not found"));
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
    note.setTotalDeduction(BigDecimal.ZERO);
    note.setReason(DebitNoteReason.WEIGHT_DIFF);
    note.setLines(new ArrayList<>());
    return debitNoteRepository.save(note);
  }

  @Transactional
  public DebitNote updateDraft(Long id, DebitNoteDtos.UpdateDebitNoteRequest request) {
    DebitNote note = debitNoteRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Debit note not found"));
    if (note.getStatus() != DocumentStatus.DRAFT) {
      throw new IllegalStateException("Only draft debit notes can be edited");
    }
    note.setDnDate(request.dnDate() != null ? request.dnDate() : LocalDate.now());
    note.setNarration(request.narration());

    List<DebitNoteLine> existingLines = debitNoteLineRepository.findByDebitNoteId(note.getId());
    if (!existingLines.isEmpty()) {
      debitNoteLineRepository.deleteAll(existingLines);
    }

    BigDecimal total = BigDecimal.ZERO;
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
        debitNoteLineRepository.save(line);
      }
    }
    note.setTotalDeduction(total);
    return debitNoteRepository.save(note);
  }

  @Transactional
  public DebitNote post(Long id) {
    DebitNote note = debitNoteRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Debit note not found"));
    if (note.getStatus() == DocumentStatus.POSTED) {
      return note;
    }
    note.setStatus(DocumentStatus.POSTED);
    return debitNoteRepository.save(note);
  }

  private String resolveDnNo(String provided) {
    if (provided != null && !provided.isBlank()) {
      return provided;
    }
    return "DN-" + LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + System.nanoTime();
  }
}
