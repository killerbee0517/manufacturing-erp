package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Customer;
import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.domain.Item;
import com.manufacturing.erp.domain.SalesOrder;
import com.manufacturing.erp.domain.SalesOrderLine;
import com.manufacturing.erp.domain.Uom;
import com.manufacturing.erp.dto.TransactionDtos;
import com.manufacturing.erp.repository.CustomerRepository;
import com.manufacturing.erp.repository.ItemRepository;
import com.manufacturing.erp.repository.SalesOrderLineRepository;
import com.manufacturing.erp.repository.SalesOrderRepository;
import com.manufacturing.erp.repository.UomRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SalesOrderService {
  private final SalesOrderRepository salesOrderRepository;
  private final SalesOrderLineRepository salesOrderLineRepository;
  private final CustomerRepository customerRepository;
  private final ItemRepository itemRepository;
  private final UomRepository uomRepository;

  public SalesOrderService(SalesOrderRepository salesOrderRepository,
                           SalesOrderLineRepository salesOrderLineRepository,
                           CustomerRepository customerRepository,
                           ItemRepository itemRepository,
                           UomRepository uomRepository) {
    this.salesOrderRepository = salesOrderRepository;
    this.salesOrderLineRepository = salesOrderLineRepository;
    this.customerRepository = customerRepository;
    this.itemRepository = itemRepository;
    this.uomRepository = uomRepository;
  }

  @org.springframework.transaction.annotation.Transactional(readOnly = true)
  public List<TransactionDtos.SalesOrderResponse> list(String status) {
    return salesOrderRepository.findAll().stream()
        .filter(so -> status == null || status.isBlank() || so.getStatus().name().equalsIgnoreCase(status))
        .map(this::toResponse)
        .toList();
  }

  @org.springframework.transaction.annotation.Transactional(readOnly = true)
  public TransactionDtos.SalesOrderResponse get(Long id) {
    SalesOrder salesOrder = salesOrderRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Sales order not found"));
    return toResponse(salesOrder);
  }

  @Transactional
  public TransactionDtos.SalesOrderResponse save(TransactionDtos.SalesOrderRequest request) {
    Customer customer = customerRepository.findById(request.customerId())
        .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

    SalesOrder salesOrder = request.id() != null
        ? salesOrderRepository.findById(request.id()).orElse(new SalesOrder())
        : new SalesOrder();
    salesOrder.setSoNo(request.soNo() != null ? request.soNo() : generateSoNo());
    salesOrder.setCustomer(customer);
    salesOrder.setOrderDate(request.orderDate());
    salesOrder.setNarration(request.narration());
    DocumentStatus status = (request.status() == null || request.status().isBlank())
        ? DocumentStatus.DRAFT
        : DocumentStatus.valueOf(request.status());
    salesOrder.setStatus(status);

    List<SalesOrderLine> existing = salesOrder.getLines();
    existing.clear();
    List<SalesOrderLine> lines = new ArrayList<>();
    for (TransactionDtos.SalesOrderLineRequest lineRequest : request.lines()) {
      SalesOrderLine line = lineRequest.id() != null
          ? salesOrderLineRepository.findById(lineRequest.id()).orElse(new SalesOrderLine())
          : new SalesOrderLine();
      line.setSalesOrder(salesOrder);
      line.setItem(itemRepository.findById(lineRequest.itemId())
          .orElseThrow(() -> new IllegalArgumentException("Item not found")));
      line.setUom(uomRepository.findById(lineRequest.uomId())
          .orElseThrow(() -> new IllegalArgumentException("UOM not found")));
      line.setQuantity(lineRequest.quantity());
      line.setRate(lineRequest.rate());
      lines.add(line);
    }
    salesOrder.setLines(lines);
    SalesOrder saved = salesOrderRepository.save(salesOrder);
    salesOrderLineRepository.saveAll(lines);
    return toResponse(saved);
  }

  private String generateSoNo() {
    return "SO-" + System.currentTimeMillis();
  }

  private TransactionDtos.SalesOrderResponse toResponse(SalesOrder salesOrder) {
    List<TransactionDtos.SalesOrderLineResponse> lines = salesOrder.getLines().stream()
        .map(line -> new TransactionDtos.SalesOrderLineResponse(
            line.getId(),
            line.getItem() != null ? line.getItem().getId() : null,
            line.getUom() != null ? line.getUom().getId() : null,
            line.getQuantity(),
            line.getRate()))
        .toList();

    return new TransactionDtos.SalesOrderResponse(
        salesOrder.getId(),
        salesOrder.getSoNo(),
        salesOrder.getCustomer() != null ? salesOrder.getCustomer().getId() : null,
        salesOrder.getOrderDate(),
        salesOrder.getStatus().name(),
        salesOrder.getNarration(),
        lines);
  }
}
