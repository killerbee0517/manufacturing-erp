package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Delivery;
import com.manufacturing.erp.domain.SalesOrder;
import com.manufacturing.erp.dto.TransactionDtos;
import com.manufacturing.erp.repository.DeliveryRepository;
import com.manufacturing.erp.repository.SalesOrderRepository;
import com.manufacturing.erp.security.CompanyContext;
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
  private final CompanyContext companyContext;

  public DeliveryController(DeliveryRepository deliveryRepository,
                            SalesOrderRepository salesOrderRepository,
                            CompanyContext companyContext) {
    this.deliveryRepository = deliveryRepository;
    this.salesOrderRepository = salesOrderRepository;
    this.companyContext = companyContext;
  }

  @GetMapping
  public List<TransactionDtos.DeliveryResponse> list() {
    Long companyId = requireCompanyId();
    return deliveryRepository.findByCompanyId(companyId).stream()
        .map(delivery -> new TransactionDtos.DeliveryResponse(
            delivery.getId(),
            delivery.getDeliveryNo(),
            delivery.getSalesOrder() != null ? delivery.getSalesOrder().getId() : null))
        .toList();
  }

  @PostMapping
  public TransactionDtos.DeliveryResponse create(@Valid @RequestBody TransactionDtos.DeliveryRequest request) {
    Long companyId = requireCompanyId();
    SalesOrder salesOrder = salesOrderRepository.findByIdAndCompanyId(request.salesOrderId(), companyId)
        .orElseThrow(() -> new IllegalArgumentException("Sales order not found"));
    Delivery delivery = new Delivery();
    delivery.setDeliveryNo(request.deliveryNo());
    delivery.setSalesOrder(salesOrder);
    delivery.setCompany(salesOrder.getCompany());
    Delivery saved = deliveryRepository.save(delivery);
    return new TransactionDtos.DeliveryResponse(saved.getId(), saved.getDeliveryNo(), salesOrder.getId());
  }

  private Long requireCompanyId() {
    Long companyId = companyContext.getCompanyId();
    if (companyId == null) {
      throw new IllegalArgumentException("Missing company context");
    }
    return companyId;
  }
}
