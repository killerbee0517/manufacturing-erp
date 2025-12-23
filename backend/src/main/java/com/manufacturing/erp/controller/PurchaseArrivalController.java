package com.manufacturing.erp.controller;

import com.manufacturing.erp.dto.PurchaseArrivalDtos;
import com.manufacturing.erp.repository.PurchaseArrivalRepository;
import com.manufacturing.erp.service.PurchaseArrivalService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/purchase-arrivals")
public class PurchaseArrivalController {
  private final PurchaseArrivalService purchaseArrivalService;
  private final PurchaseArrivalRepository purchaseArrivalRepository;

  public PurchaseArrivalController(PurchaseArrivalService purchaseArrivalService,
                                   PurchaseArrivalRepository purchaseArrivalRepository) {
    this.purchaseArrivalService = purchaseArrivalService;
    this.purchaseArrivalRepository = purchaseArrivalRepository;
  }

  @GetMapping
  public List<PurchaseArrivalDtos.PurchaseArrivalResponse> list() {
    return purchaseArrivalRepository.findAll().stream()
        .map(this::toResponse)
        .toList();
  }

  @GetMapping("/{id}")
  public PurchaseArrivalDtos.PurchaseArrivalResponse get(@PathVariable Long id) {
    var arrival = purchaseArrivalRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Purchase arrival not found"));
    return toResponse(arrival);
  }

  @PostMapping
  public PurchaseArrivalDtos.PurchaseArrivalResponse create(
      @Valid @RequestBody PurchaseArrivalDtos.CreatePurchaseArrivalRequest request) {
    var arrival = purchaseArrivalService.createArrival(request);
    return toResponse(arrival);
  }

  private PurchaseArrivalDtos.PurchaseArrivalResponse toResponse(com.manufacturing.erp.domain.PurchaseArrival arrival) {
    return new PurchaseArrivalDtos.PurchaseArrivalResponse(
        arrival.getId(),
        arrival.getPurchaseOrder() != null ? arrival.getPurchaseOrder().getId() : null,
        arrival.getWeighbridgeTicket() != null ? arrival.getWeighbridgeTicket().getId() : null,
        arrival.getGodown() != null ? arrival.getGodown().getId() : null,
        arrival.getUnloadingCharges(),
        arrival.getDeductions(),
        arrival.getTdsPercent(),
        arrival.getGrossAmount(),
        arrival.getNetPayable(),
        arrival.getCreatedAt());
  }
}
