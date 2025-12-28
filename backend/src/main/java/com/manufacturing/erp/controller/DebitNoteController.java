package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.DebitNote;
import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.dto.DebitNoteDtos;
import com.manufacturing.erp.repository.DebitNoteLineRepository;
import com.manufacturing.erp.repository.DebitNoteRepository;
import com.manufacturing.erp.service.DebitNoteService;
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
@RequestMapping("/api/debit-notes")
public class DebitNoteController {
  private final DebitNoteService debitNoteService;
  private final DebitNoteRepository debitNoteRepository;
  private final DebitNoteLineRepository debitNoteLineRepository;

  public DebitNoteController(DebitNoteService debitNoteService,
                             DebitNoteRepository debitNoteRepository,
                             DebitNoteLineRepository debitNoteLineRepository) {
    this.debitNoteService = debitNoteService;
    this.debitNoteRepository = debitNoteRepository;
    this.debitNoteLineRepository = debitNoteLineRepository;
  }

  @GetMapping
  public List<DebitNoteDtos.DebitNoteResponse> list(@RequestParam(required = false) String status) {
    var notes = debitNoteRepository.findAll();
    if (status != null && !status.isBlank()) {
      DocumentStatus filter = DocumentStatus.valueOf(status.toUpperCase());
      notes = notes.stream().filter(note -> note.getStatus() == filter).toList();
    }
    return notes.stream().map(this::toResponse).toList();
  }

  @GetMapping("/{id}")
  public DebitNoteDtos.DebitNoteResponse get(@PathVariable Long id) {
    var note = debitNoteRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Debit note not found"));
    return toResponse(note);
  }

  @PostMapping("/from-invoice/{invoiceId}")
  public DebitNoteDtos.DebitNoteResponse createFromInvoice(@PathVariable Long invoiceId) {
    var note = debitNoteService.createFromInvoice(invoiceId);
    return toResponse(note);
  }

  @PutMapping("/{id}")
  public DebitNoteDtos.DebitNoteResponse update(@PathVariable Long id,
                                                @RequestBody DebitNoteDtos.UpdateDebitNoteRequest request) {
    var note = debitNoteService.updateDraft(id, request);
    return toResponse(note);
  }

  @PostMapping("/{id}/post")
  public DebitNoteDtos.DebitNoteResponse post(@PathVariable Long id) {
    var note = debitNoteService.post(id);
    return toResponse(note);
  }

  private DebitNoteDtos.DebitNoteResponse toResponse(DebitNote note) {
    var lines = debitNoteLineRepository.findByDebitNoteId(note.getId());
    List<DebitNoteDtos.DebitNoteLineResponse> lineResponses = lines.stream()
        .map(line -> new DebitNoteDtos.DebitNoteLineResponse(
            line.getId(),
            line.getRuleId(),
            line.getDescription(),
            line.getBaseValue(),
            line.getRate(),
            line.getAmount(),
            line.getRemarks()))
        .toList();
    return new DebitNoteDtos.DebitNoteResponse(
        note.getId(),
        note.getDebitNoteNo(),
        note.getSupplier() != null ? note.getSupplier().getId() : null,
        note.getSupplier() != null ? note.getSupplier().getName() : null,
        note.getPurchaseInvoice() != null ? note.getPurchaseInvoice().getId() : null,
        note.getPurchaseInvoice() != null ? note.getPurchaseInvoice().getInvoiceNo() : null,
        note.getPurchaseOrder() != null ? note.getPurchaseOrder().getId() : null,
        note.getPurchaseOrder() != null ? note.getPurchaseOrder().getPoNo() : null,
        note.getGrn() != null ? note.getGrn().getId() : null,
        note.getGrn() != null ? note.getGrn().getGrnNo() : null,
        note.getDnDate(),
        note.getNarration(),
        note.getStatus() != null ? note.getStatus().name() : null,
        note.getTotalDeduction(),
        lineResponses
    );
  }
}
