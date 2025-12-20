package com.manufacturing.erp.controller;

import com.manufacturing.erp.dto.StockTransferDtos;
import com.manufacturing.erp.service.StockTransferService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock-transfers")
public class StockTransferController {
  private final StockTransferService stockTransferService;

  public StockTransferController(StockTransferService stockTransferService) {
    this.stockTransferService = stockTransferService;
  }

  @PostMapping
  public void create(@Valid @RequestBody StockTransferDtos.TransferRequest request) {
    stockTransferService.transfer(request);
  }
}
