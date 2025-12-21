package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Delivery;
import com.manufacturing.erp.domain.SalesOrder;
import com.manufacturing.erp.dto.TransactionDtos;
import com.manufacturing.erp.repository.DeliveryRepository;
import com.manufacturing.erp.repository.SalesOrderRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {
  private final DeliveryRepository deliveryRepository;
  private final SalesOrderRepository salesOrderRepository;

  public DeliveryController(DeliveryRepository deliveryRepository, SalesOrderRepository salesOrderRepository) {
    this.deliveryRepository = deliveryRepository;
    this.salesOrderRepository = salesOrderRepository;
  }

  @GetMapping
  public List<TransactionDtos.DeliveryResponse> list() {
    return deliveryRepository.findAll().stream()
        .map(delivery -> new TransactionDtos.DeliveryResponse(
            delivery.getId(),
            delivery.getDeliveryNo(),
            delivery.getSalesOrder() != null ? delivery.getSalesOrder().getId() : null))
        .toList();
  }

  @PostMapping
  public TransactionDtos.DeliveryResponse create(@Valid @RequestBody TransactionDtos.DeliveryRequest request) {
    SalesOrder salesOrder = salesOrderRepository.findById(request.salesOrderId())
        .orElseThrow(() -> new IllegalArgumentException("Sales order not found"));
    Delivery delivery = new Delivery();
    delivery.setDeliveryNo(request.deliveryNo());
    delivery.setSalesOrder(salesOrder);
    Delivery saved = deliveryRepository.save(delivery);
    return new TransactionDtos.DeliveryResponse(saved.getId(), saved.getDeliveryNo(), salesOrder.getId());
  }
}
