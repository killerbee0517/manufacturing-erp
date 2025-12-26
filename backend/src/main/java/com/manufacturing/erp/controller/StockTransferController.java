package com.manufacturing.erp.controller;

import com.manufacturing.erp.dto.StockTransferDtos;
import com.manufacturing.erp.service.StockTransferService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/stock-transfers")
public class StockTransferController {
  private final StockTransferService stockTransferService;

  public StockTransferController(StockTransferService stockTransferService) {
    this.stockTransferService = stockTransferService;
  }

  @GetMapping
  public List<StockTransferDtos.StockTransferResponse> list(@RequestParam(required = false) String status) {
    return stockTransferService.list(status);
  }

  @GetMapping("/{id}")
  public StockTransferDtos.StockTransferResponse get(@PathVariable Long id) {
    return stockTransferService.get(id);
  }

  @PostMapping
  public StockTransferDtos.StockTransferResponse create(@Valid @RequestBody StockTransferDtos.StockTransferRequest request) {
    return stockTransferService.save(request);
  }

  @PutMapping("/{id}")
  public StockTransferDtos.StockTransferResponse update(@PathVariable Long id,
                                                        @Valid @RequestBody StockTransferDtos.StockTransferRequest request) {
    return stockTransferService.save(new StockTransferDtos.StockTransferRequest(
        id,
        request.transferNo(),
        request.fromGodownId(),
        request.toGodownId(),
        request.transferDate(),
        request.narration(),
        request.lines()));
  }

  @PostMapping("/post")
  public StockTransferDtos.StockTransferResponse post(@Valid @RequestBody StockTransferDtos.StockTransferPostRequest request) {
    return stockTransferService.post(request.id());
  }
}
