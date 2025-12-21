package com.manufacturing.erp.controller;

import com.manufacturing.erp.dto.StockTransferDtos;
import com.manufacturing.erp.repository.StockLedgerRepository;
import com.manufacturing.erp.service.StockTransferService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock-transfers")
public class StockTransferController {
  private final StockTransferService stockTransferService;
  private final StockLedgerRepository stockLedgerRepository;

  public StockTransferController(StockTransferService stockTransferService,
                                 StockLedgerRepository stockLedgerRepository) {
    this.stockTransferService = stockTransferService;
    this.stockLedgerRepository = stockLedgerRepository;
  }

  @GetMapping
  public List<StockTransferResponse> list() {
    return stockLedgerRepository.findByDocType("STOCK_TRANSFER").stream()
        .map(entry -> new StockTransferResponse(
            entry.getId(),
            entry.getItem() != null ? entry.getItem().getId() : null,
            entry.getFromLocation() != null ? entry.getFromLocation().getId() : null,
            entry.getToLocation() != null ? entry.getToLocation().getId() : null,
            entry.getQuantity(),
            entry.getWeight(),
            entry.getStatus().name()))
        .toList();
  }

  @PostMapping
  public void create(@Valid @RequestBody StockTransferDtos.TransferRequest request) {
    stockTransferService.transfer(request);
  }

  public record StockTransferResponse(
      Long id,
      Long itemId,
      Long fromLocationId,
      Long toLocationId,
      java.math.BigDecimal quantity,
      java.math.BigDecimal weight,
      String status) {}
}
