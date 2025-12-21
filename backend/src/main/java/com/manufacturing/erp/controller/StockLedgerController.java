package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.StockLedger;
import com.manufacturing.erp.repository.StockLedgerRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock-ledger")
public class StockLedgerController {
  private final StockLedgerRepository stockLedgerRepository;

  public StockLedgerController(StockLedgerRepository stockLedgerRepository) {
    this.stockLedgerRepository = stockLedgerRepository;
  }

  @GetMapping
  public List<StockLedgerResponse> list() {
    return stockLedgerRepository.findAll().stream()
        .map(entry -> new StockLedgerResponse(
            entry.getId(),
            entry.getDocType(),
            entry.getDocId(),
            entry.getTxnType().name(),
            entry.getQuantity(),
            entry.getWeight(),
            entry.getItem() != null ? entry.getItem().getId() : null,
            entry.getFromLocation() != null ? entry.getFromLocation().getId() : null,
            entry.getToLocation() != null ? entry.getToLocation().getId() : null,
            entry.getStatus().name()))
        .toList();
  }

  public record StockLedgerResponse(
      Long id,
      String docType,
      Long docId,
      String txnType,
      java.math.BigDecimal quantity,
      java.math.BigDecimal weight,
      Long itemId,
      Long fromLocationId,
      Long toLocationId,
      String status) {}
}
