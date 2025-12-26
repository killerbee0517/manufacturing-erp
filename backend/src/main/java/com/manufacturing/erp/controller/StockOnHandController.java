package com.manufacturing.erp.controller;

import com.manufacturing.erp.dto.StockDtos;
import com.manufacturing.erp.service.StockQueryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stock-on-hand")
public class StockOnHandController {
  private final StockQueryService stockQueryService;

  public StockOnHandController(StockQueryService stockQueryService) {
    this.stockQueryService = stockQueryService;
  }

  @GetMapping
  public List<StockDtos.StockOnHandResponse> list(@RequestParam(required = false) Long godownId,
                                                  @RequestParam(required = false) Long itemId) {
    return stockQueryService.getStockOnHand(godownId, itemId);
  }
}
