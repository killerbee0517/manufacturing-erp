package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.DebitNote;
import com.manufacturing.erp.domain.Enums.DebitNoteReason;
import com.manufacturing.erp.domain.Supplier;
import com.manufacturing.erp.dto.TransactionDtos;
import com.manufacturing.erp.repository.DebitNoteRepository;
import com.manufacturing.erp.repository.SupplierRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/debit-notes")
public class DebitNoteController {
  private final DebitNoteRepository debitNoteRepository;
  private final SupplierRepository supplierRepository;

  public DebitNoteController(DebitNoteRepository debitNoteRepository, SupplierRepository supplierRepository) {
    this.debitNoteRepository = debitNoteRepository;
    this.supplierRepository = supplierRepository;
  }

  @GetMapping
  public List<TransactionDtos.DebitNoteResponse> list() {
    return debitNoteRepository.findAll().stream()
        .map(note -> new TransactionDtos.DebitNoteResponse(
            note.getId(),
            note.getDebitNoteNo(),
            note.getSupplier() != null ? note.getSupplier().getId() : null,
            note.getReason().name()))
        .toList();
  }

  @PostMapping
  public TransactionDtos.DebitNoteResponse create(@Valid @RequestBody TransactionDtos.DebitNoteRequest request) {
    Supplier supplier = supplierRepository.findById(request.supplierId())
        .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));
    DebitNote note = new DebitNote();
    note.setDebitNoteNo(request.debitNoteNo());
    note.setSupplier(supplier);
    note.setReason(DebitNoteReason.valueOf(request.reason().toUpperCase()));
    DebitNote saved = debitNoteRepository.save(note);
    return new TransactionDtos.DebitNoteResponse(saved.getId(), saved.getDebitNoteNo(), supplier.getId(), saved.getReason().name());
  }
}
