package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.StockLedger;
import com.manufacturing.erp.dto.StockDtos;
import com.manufacturing.erp.repository.StockLedgerRepository;
import com.manufacturing.erp.security.CompanyContext;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock-ledger")
public class StockLedgerController {
  private final StockLedgerRepository stockLedgerRepository;
  private final CompanyContext companyContext;

  public StockLedgerController(StockLedgerRepository stockLedgerRepository,
                               CompanyContext companyContext) {
    this.stockLedgerRepository = stockLedgerRepository;
    this.companyContext = companyContext;
  }

  @GetMapping
  public List<StockDtos.StockLedgerResponse> list(@RequestParam(required = false) Long itemId,
                                                  @RequestParam(required = false) Long godownId,
                                                  @RequestParam(required = false) LocalDate from,
                                                  @RequestParam(required = false) LocalDate to) {
    Long companyId = requireCompanyId();
    Instant fromInstant = from != null ? from.atStartOfDay().toInstant(ZoneOffset.UTC) : null;
    Instant toInstant = to != null ? to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC) : null;
    return stockLedgerRepository.findLedger(companyId, itemId, godownId, fromInstant, toInstant).stream()
        .map(this::toResponse)
        .toList();
  }

  private StockDtos.StockLedgerResponse toResponse(StockLedger entry) {
    return new StockDtos.StockLedgerResponse(
        entry.getId(),
        entry.getDocType(),
        entry.getDocId(),
        entry.getDocLineId(),
        entry.getItem() != null ? entry.getItem().getId() : null,
        entry.getGodown() != null ? entry.getGodown().getId() : null,
        entry.getFromGodown() != null ? entry.getFromGodown().getId() : null,
        entry.getToGodown() != null ? entry.getToGodown().getId() : null,
        entry.getQtyIn(),
        entry.getQtyOut(),
        entry.getRate(),
        entry.getAmount(),
        entry.getStatus().name(),
        entry.getTxnType().name(),
        entry.getPostedAt());
  }

  private Long requireCompanyId() {
    Long companyId = companyContext.getCompanyId();
    if (companyId == null) {
      throw new IllegalArgumentException("Missing company context");
    }
    return companyId;
  }
}
