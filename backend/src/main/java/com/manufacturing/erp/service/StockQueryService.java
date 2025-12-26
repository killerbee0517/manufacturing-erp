package com.manufacturing.erp.service;

import com.manufacturing.erp.dto.StockDtos;
import com.manufacturing.erp.repository.StockLedgerRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class StockQueryService {
  private final StockLedgerRepository stockLedgerRepository;

  public StockQueryService(StockLedgerRepository stockLedgerRepository) {
    this.stockLedgerRepository = stockLedgerRepository;
  }

  public List<StockDtos.StockOnHandResponse> getStockOnHand(Long godownId, Long itemId) {
    List<com.manufacturing.erp.domain.StockLedger> ledger = stockLedgerRepository.findLedger(itemId, godownId, null, null);
    record Totals(BigDecimal in, BigDecimal out) {}
    java.util.Map<String, Totals> balanceMap = new java.util.HashMap<>();
    for (var entry : ledger) {
      Long item = entry.getItem() != null ? entry.getItem().getId() : null;
      Long godown = entry.getGodown() != null ? entry.getGodown().getId() : null;
      String key = item + "-" + godown;
      Totals current = balanceMap.getOrDefault(key, new Totals(BigDecimal.ZERO, BigDecimal.ZERO));
      balanceMap.put(key, new Totals(
          current.in().add(entry.getQtyIn()),
          current.out().add(entry.getQtyOut())
      ));
    }
    return balanceMap.entrySet().stream()
        .map(e -> {
          String[] parts = e.getKey().split("-");
          Long itemKey = parts[0].equals("null") ? null : Long.parseLong(parts[0]);
          Long godownKey = parts[1].equals("null") ? null : Long.parseLong(parts[1]);
          Totals totals = e.getValue();
          return new StockDtos.StockOnHandResponse(
              itemKey,
              godownKey,
              totals.in(),
              totals.out(),
              totals.in().subtract(totals.out()));
        })
        .toList();
  }
}
