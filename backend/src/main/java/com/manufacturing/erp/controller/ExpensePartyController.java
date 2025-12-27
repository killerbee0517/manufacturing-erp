package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Enums.PayablePartyType;
import com.manufacturing.erp.domain.ExpenseParty;
import com.manufacturing.erp.dto.ExpensePartyDtos;
import com.manufacturing.erp.repository.ExpensePartyRepository;
import com.manufacturing.erp.service.LedgerService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expense-parties")
public class ExpensePartyController {
  private final ExpensePartyRepository repository;
  private final LedgerService ledgerService;

  public ExpensePartyController(ExpensePartyRepository repository, LedgerService ledgerService) {
    this.repository = repository;
    this.ledgerService = ledgerService;
  }

  @GetMapping
  public List<ExpensePartyDtos.ExpensePartyResponse> list() {
    return repository.findAll().stream().map(this::toResponse).toList();
  }

  @GetMapping("/{id}")
  public ExpensePartyDtos.ExpensePartyResponse get(@PathVariable Long id) {
    ExpenseParty entity = repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Expense party not found"));
    return toResponse(entity);
  }

  @PostMapping
  public ExpensePartyDtos.ExpensePartyResponse create(@Valid @RequestBody ExpensePartyDtos.ExpensePartyRequest request) {
    ExpenseParty entity = new ExpenseParty();
    apply(entity, request);
    ensureLedger(entity);
    return toResponse(repository.save(entity));
  }

  @PutMapping("/{id}")
  public ExpensePartyDtos.ExpensePartyResponse update(@PathVariable Long id,
                                                      @Valid @RequestBody ExpensePartyDtos.ExpensePartyRequest request) {
    ExpenseParty entity = repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Expense party not found"));
    apply(entity, request);
    ensureLedger(entity);
    return toResponse(repository.save(entity));
  }

  private void apply(ExpenseParty entity, ExpensePartyDtos.ExpensePartyRequest request) {
    entity.setName(request.name());
    entity.setPartyType(PayablePartyType.valueOf(request.partyType().toUpperCase()));
  }

  private void ensureLedger(ExpenseParty entity) {
    if (entity.getLedger() != null) {
      return;
    }
    var ledger = ledgerService.findOrCreateLedger(entity.getName(), com.manufacturing.erp.domain.Enums.LedgerType.GENERAL);
    entity.setLedger(ledger);
  }

  private ExpensePartyDtos.ExpensePartyResponse toResponse(ExpenseParty entity) {
    return new ExpensePartyDtos.ExpensePartyResponse(
        entity.getId(),
        entity.getName(),
        entity.getPartyType() != null ? entity.getPartyType().name() : null,
        entity.getLedger() != null ? entity.getLedger().getId() : null);
  }
}
