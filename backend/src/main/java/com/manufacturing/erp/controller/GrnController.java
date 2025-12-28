package com.manufacturing.erp.controller;

import com.manufacturing.erp.dto.GrnDtos;
import com.manufacturing.erp.repository.GrnRepository;
import com.manufacturing.erp.repository.WeighbridgeTicketRepository;
import com.manufacturing.erp.service.GrnService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/grn")
public class GrnController {
  private final GrnService grnService;
  private final GrnRepository grnRepository;
  private final WeighbridgeTicketRepository weighbridgeTicketRepository;

  public GrnController(GrnService grnService, GrnRepository grnRepository,
                       WeighbridgeTicketRepository weighbridgeTicketRepository) {
    this.grnService = grnService;
    this.grnRepository = grnRepository;
    this.weighbridgeTicketRepository = weighbridgeTicketRepository;
  }

  @GetMapping
  public List<GrnDtos.GrnResponse> list(@RequestParam(required = false) Long poId,
                                        @RequestParam(required = false) Long weighbridgeId,
                                        @RequestParam(required = false) String status) {
    var source = grnRepository.findAll();
    if (poId != null) {
      source = source.stream()
          .filter(grn -> grn.getPurchaseOrder() != null && grn.getPurchaseOrder().getId().equals(poId))
          .toList();
    }
    if (weighbridgeId != null) {
      source = source.stream()
          .filter(grn -> grn.getWeighbridgeTicket() != null && grn.getWeighbridgeTicket().getId().equals(weighbridgeId))
          .toList();
    }
    if (status != null && !status.isBlank()) {
      var filter = com.manufacturing.erp.domain.Enums.DocumentStatus.valueOf(status.toUpperCase());
      source = source.stream().filter(grn -> grn.getStatus() == filter).toList();
    }
    return source.stream().map(this::toResponse).toList();
  }

  @GetMapping("/{id}")
  public GrnDtos.GrnResponse get(@PathVariable Long id) {
    var grn = grnRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("GRN not found"));
    return toResponse(grn);
  }

  @PostMapping
  public GrnDtos.GrnResponse create(@Valid @RequestBody GrnDtos.CreateGrnRequest request) {
    var grn = grnService.createGrn(request);
    return toResponse(grn);
  }

  @PostMapping("/from-weighbridge/{weighbridgeId}")
  public GrnDtos.GrnResponse createFromWeighbridge(@PathVariable Long weighbridgeId) {
    var ticket = weighbridgeTicketRepository.findById(weighbridgeId)
        .orElseThrow(() -> new IllegalArgumentException("Weighbridge ticket not found"));
    var grn = grnService.createDraftFromWeighbridge(ticket);
    return toResponse(grn);
  }

  @PutMapping("/{id}")
  public GrnDtos.GrnResponse update(@PathVariable Long id, @RequestBody GrnDtos.UpdateGrnRequest request) {
    var grn = grnService.updateDraft(id, request);
    return toResponse(grn);
  }

  @PostMapping("/{id}/post")
  public GrnDtos.GrnResponse post(@PathVariable Long id) {
    var grn = grnService.post(id);
    return toResponse(grn);
  }

  private GrnDtos.GrnResponse toResponse(com.manufacturing.erp.domain.Grn grn) {
    List<GrnDtos.GrnLineResponse> lines = grn.getLines().stream()
        .map(line -> new GrnDtos.GrnLineResponse(
            line.getId(),
            line.getItem() != null ? line.getItem().getId() : null,
            line.getItem() != null ? line.getItem().getName() : null,
            line.getUom() != null ? line.getUom().getId() : null,
            line.getUom() != null ? line.getUom().getCode() : null,
            line.getExpectedQty(),
            line.getReceivedQty(),
            line.getAcceptedQty(),
            line.getRejectedQty(),
            line.getWeight(),
            line.getRate(),
            line.getAmount()))
        .toList();
    return new GrnDtos.GrnResponse(
        grn.getId(),
        grn.getGrnNo(),
        grn.getSupplier() != null ? grn.getSupplier().getId() : null,
        grn.getSupplier() != null ? grn.getSupplier().getName() : null,
        grn.getPurchaseOrder() != null ? grn.getPurchaseOrder().getId() : null,
        grn.getPurchaseOrder() != null ? grn.getPurchaseOrder().getPoNo() : null,
        grn.getWeighbridgeTicket() != null ? grn.getWeighbridgeTicket().getId() : null,
        grn.getWeighbridgeTicket() != null ? grn.getWeighbridgeTicket().getSerialNo() : null,
        grn.getGodown() != null ? grn.getGodown().getId() : null,
        grn.getGodown() != null ? grn.getGodown().getName() : null,
        grn.getGrnDate(),
        grn.getFirstWeight(),
        grn.getSecondWeight(),
        grn.getNetWeight(),
        grn.getNarration(),
        grn.getStatus().name(),
        lines);
  }
}
