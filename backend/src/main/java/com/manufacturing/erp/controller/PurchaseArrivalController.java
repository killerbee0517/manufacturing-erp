package com.manufacturing.erp.controller;

import com.manufacturing.erp.dto.PurchaseArrivalDtos;
import com.manufacturing.erp.repository.PurchaseArrivalRepository;
import com.manufacturing.erp.service.PurchaseArrivalService;
import com.manufacturing.erp.security.CompanyContext;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/purchase-arrivals")
public class PurchaseArrivalController {
  private final PurchaseArrivalService purchaseArrivalService;
  private final PurchaseArrivalRepository purchaseArrivalRepository;
  private final CompanyContext companyContext;

  public PurchaseArrivalController(PurchaseArrivalService purchaseArrivalService,
                                   PurchaseArrivalRepository purchaseArrivalRepository,
                                   CompanyContext companyContext) {
    this.purchaseArrivalService = purchaseArrivalService;
    this.purchaseArrivalRepository = purchaseArrivalRepository;
    this.companyContext = companyContext;
  }

  @GetMapping
  public List<PurchaseArrivalDtos.PurchaseArrivalResponse> list() {
    Long companyId = companyContext.getCompanyId();
    if (companyId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing company context");
    }
    return purchaseArrivalRepository.findAllByPurchaseOrderCompanyId(companyId).stream()
        .map(this::toResponse)
        .toList();
  }

  @GetMapping("/{id}")
  public PurchaseArrivalDtos.PurchaseArrivalResponse get(@PathVariable Long id) {
    Long companyId = companyContext.getCompanyId();
    if (companyId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing company context");
    }
    var arrival = purchaseArrivalRepository.findByIdAndPurchaseOrderCompanyId(id, companyId)
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
    var charges = arrival.getCharges().stream()
        .map(charge -> new PurchaseArrivalDtos.PurchaseArrivalChargeResponse(
            charge.getId(),
            charge.getChargeType() != null ? charge.getChargeType().getId() : null,
            charge.getCalcType() != null ? charge.getCalcType().name() : null,
            charge.getRate(),
            charge.getAmount(),
            charge.isDeduction(),
            charge.getPayablePartyType() != null ? charge.getPayablePartyType().name() : null,
            charge.getPayablePartyId(),
            charge.getRemarks()
        ))
        .toList();
    return new PurchaseArrivalDtos.PurchaseArrivalResponse(
        arrival.getId(),
        arrival.getPurchaseOrder() != null ? arrival.getPurchaseOrder().getId() : null,
        arrival.getWeighbridgeTicket() != null ? arrival.getWeighbridgeTicket().getId() : null,
        arrival.getBroker() != null ? arrival.getBroker().getId() : null,
        arrival.getBroker() != null ? arrival.getBroker().getName() : null,
        arrival.getBrokerageAmount(),
        arrival.getGodown() != null ? arrival.getGodown().getId() : null,
        arrival.getGrossAmount(),
        arrival.getNetPayable(),
        charges,
        arrival.getCreatedAt());
  }
}
