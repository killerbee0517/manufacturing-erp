package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Ledger;
import com.manufacturing.erp.dto.LedgerDtos;
import com.manufacturing.erp.repository.LedgerRepository;
import com.manufacturing.erp.service.LedgerService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/ledgers")
public class LedgerController {
  private final LedgerRepository ledgerRepository;
  private final LedgerService ledgerService;

  public LedgerController(LedgerRepository ledgerRepository, LedgerService ledgerService) {
    this.ledgerRepository = ledgerRepository;
    this.ledgerService = ledgerService;
  }

  @GetMapping
  public List<LedgerDtos.LedgerResponse> list() {
    return ledgerRepository.findAll().stream()
        .map(this::toResponse)
        .toList();
  }

  @GetMapping("/{id}/balance")
  public LedgerDtos.LedgerBalanceResponse getBalance(@PathVariable Long id) {
    if (!ledgerRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ledger not found");
    }
    return new LedgerDtos.LedgerBalanceResponse(id, ledgerService.getBalance(id));
  }

  @GetMapping("/{id}/statement")
  public List<LedgerDtos.LedgerStatementEntry> getStatement(
      @PathVariable Long id,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    if (!ledgerRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Ledger not found");
    }
    return ledgerService.getStatement(id, from, to);
  }

  private LedgerDtos.LedgerResponse toResponse(Ledger ledger) {
    return new LedgerDtos.LedgerResponse(
        ledger.getId(),
        ledger.getName(),
        ledger.getType().name(),
        ledgerService.getBalance(ledger.getId())
    );
  }
}
