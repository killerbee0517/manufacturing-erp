package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Customer;
import com.manufacturing.erp.dto.TransactionDtos;
import com.manufacturing.erp.service.SalesOrderService;
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
@RequestMapping("/api/sales-orders")
public class SalesOrderController {
  private final SalesOrderService salesOrderService;

  public SalesOrderController(SalesOrderService salesOrderService) {
    this.salesOrderService = salesOrderService;
  }

  @GetMapping
  public List<TransactionDtos.SalesOrderResponse> list(@RequestParam(required = false) String status) {
    return salesOrderService.list(status);
  }

  @GetMapping("/{id}")
  public TransactionDtos.SalesOrderResponse get(@PathVariable Long id) {
    return salesOrderService.get(id);
  }

  @PostMapping
  public TransactionDtos.SalesOrderResponse create(@Valid @RequestBody TransactionDtos.SalesOrderRequest request) {
    return salesOrderService.save(request);
  }

  @PutMapping("/{id}")
  public TransactionDtos.SalesOrderResponse update(@PathVariable Long id,
                                                   @Valid @RequestBody TransactionDtos.SalesOrderRequest request) {
    return salesOrderService.save(new TransactionDtos.SalesOrderRequest(
        id,
        request.soNo(),
        request.customerId(),
        request.orderDate(),
        request.status(),
        request.narration(),
        request.lines()));
  }
}
