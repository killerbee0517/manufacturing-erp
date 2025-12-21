package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Customer;
import com.manufacturing.erp.domain.SalesOrder;
import com.manufacturing.erp.dto.TransactionDtos;
import com.manufacturing.erp.repository.CustomerRepository;
import com.manufacturing.erp.repository.SalesOrderRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sales-orders")
public class SalesOrderController {
  private final SalesOrderRepository salesOrderRepository;
  private final CustomerRepository customerRepository;

  public SalesOrderController(SalesOrderRepository salesOrderRepository, CustomerRepository customerRepository) {
    this.salesOrderRepository = salesOrderRepository;
    this.customerRepository = customerRepository;
  }

  @GetMapping
  public List<TransactionDtos.SalesOrderResponse> list() {
    return salesOrderRepository.findAll().stream()
        .map(so -> new TransactionDtos.SalesOrderResponse(
            so.getId(),
            so.getSoNo(),
            so.getCustomer() != null ? so.getCustomer().getId() : null,
            so.getStatus()))
        .toList();
  }

  @PostMapping
  public TransactionDtos.SalesOrderResponse create(@Valid @RequestBody TransactionDtos.SalesOrderRequest request) {
    Customer customer = customerRepository.findById(request.customerId())
        .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
    SalesOrder salesOrder = new SalesOrder();
    salesOrder.setSoNo(request.soNo());
    salesOrder.setCustomer(customer);
    salesOrder.setStatus(request.status() != null ? request.status() : "DRAFT");
    SalesOrder saved = salesOrderRepository.save(salesOrder);
    return new TransactionDtos.SalesOrderResponse(saved.getId(), saved.getSoNo(), customer.getId(), saved.getStatus());
  }
}
