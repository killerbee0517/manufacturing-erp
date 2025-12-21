package com.manufacturing.erp.controller;

import com.manufacturing.erp.dto.GrnDtos;
import com.manufacturing.erp.repository.GrnRepository;
import com.manufacturing.erp.service.GrnService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/grn")
public class GrnController {
  private final GrnService grnService;
  private final GrnRepository grnRepository;

  public GrnController(GrnService grnService, GrnRepository grnRepository) {
    this.grnService = grnService;
    this.grnRepository = grnRepository;
  }

  @GetMapping
  public List<GrnDtos.GrnResponse> list() {
    return grnRepository.findAll().stream()
        .map(grn -> new GrnDtos.GrnResponse(
            grn.getId(),
            grn.getGrnNo(),
            grn.getSupplier() != null ? grn.getSupplier().getId() : null,
            grn.getPurchaseOrder() != null ? grn.getPurchaseOrder().getId() : null,
            grn.getWeighbridgeTicket() != null ? grn.getWeighbridgeTicket().getId() : null,
            grn.getGrnDate(),
            grn.getItem() != null ? grn.getItem().getId() : null,
            grn.getUom() != null ? grn.getUom().getId() : null,
            grn.getQuantity(),
            grn.getFirstWeight(),
            grn.getSecondWeight(),
            grn.getNetWeight(),
            grn.getNarration(),
            grn.getStatus().name()))
        .toList();
  }

  @PostMapping
  public GrnDtos.GrnResponse create(@Valid @RequestBody GrnDtos.CreateGrnRequest request) {
    var grn = grnService.createGrn(request);
    return new GrnDtos.GrnResponse(
        grn.getId(),
        grn.getGrnNo(),
        grn.getSupplier() != null ? grn.getSupplier().getId() : null,
        grn.getPurchaseOrder() != null ? grn.getPurchaseOrder().getId() : null,
        grn.getWeighbridgeTicket() != null ? grn.getWeighbridgeTicket().getId() : null,
        grn.getGrnDate(),
        grn.getItem() != null ? grn.getItem().getId() : null,
        grn.getUom() != null ? grn.getUom().getId() : null,
        grn.getQuantity(),
        grn.getFirstWeight(),
        grn.getSecondWeight(),
        grn.getNetWeight(),
        grn.getNarration(),
        grn.getStatus().name());
  }
}
