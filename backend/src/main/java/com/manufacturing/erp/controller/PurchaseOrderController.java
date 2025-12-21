package com.manufacturing.erp.controller;

import com.manufacturing.erp.dto.TransactionDtos;
import com.manufacturing.erp.service.PurchaseOrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {
  private final PurchaseOrderService purchaseOrderService;

  public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
    this.purchaseOrderService = purchaseOrderService;
  }

  @GetMapping
  public Page<TransactionDtos.PurchaseOrderResponse> list(@RequestParam(required = false) String q,
                                                          @RequestParam(required = false) String status,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size,
                                                          @RequestParam(defaultValue = "id,desc") String sort) {
    PageRequest pageRequest = PageRequest.of(page, size, parseSort(sort));
    return purchaseOrderService.list(q, status, pageRequest);
  }

  @GetMapping("/{id}")
  public TransactionDtos.PurchaseOrderResponse getById(@PathVariable Long id) {
    return purchaseOrderService.getById(id);
  }

  @PostMapping
  public TransactionDtos.PurchaseOrderResponse create(@Valid @RequestBody TransactionDtos.PurchaseOrderRequest request) {
    return purchaseOrderService.create(request);
  }

  @PutMapping("/{id}")
  public TransactionDtos.PurchaseOrderResponse update(@PathVariable Long id,
                                                      @Valid @RequestBody TransactionDtos.PurchaseOrderRequest request) {
    return purchaseOrderService.update(id, request);
  }

  @PostMapping("/{id}/approve")
  public TransactionDtos.PurchaseOrderResponse approve(@PathVariable Long id) {
    return purchaseOrderService.approve(id);
  }

  private Sort parseSort(String sort) {
    String[] parts = sort.split(",");
    String field = parts.length > 0 ? parts[0] : "id";
    String direction = parts.length > 1 ? parts[1] : "desc";
    return "asc".equalsIgnoreCase(direction) ? Sort.by(field).ascending() : Sort.by(field).descending();
  }
}
