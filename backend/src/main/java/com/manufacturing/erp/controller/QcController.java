package com.manufacturing.erp.controller;

import com.manufacturing.erp.dto.QcDtos;
import com.manufacturing.erp.service.QcService;
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
@RequestMapping("/api/qc")
public class QcController {
  private final QcService qcService;

  public QcController(QcService qcService) {
    this.qcService = qcService;
  }

  @GetMapping("/inspections")
  public List<QcDtos.QcResponse> list(@RequestParam(required = false) Long grnId,
                                      @RequestParam(required = false) Long weighbridgeId,
                                      @RequestParam(required = false) Long poId) {
    return qcService.list(grnId, weighbridgeId, poId).stream().map(this::toResponse).toList();
  }

  @PostMapping("/inspections/from-weighbridge/{weighbridgeId}")
  public QcDtos.QcResponse createFromWeighbridge(@PathVariable Long weighbridgeId) {
    return toResponse(qcService.createDraftFromWeighbridge(weighbridgeId));
  }

  @GetMapping("/inspections/{id}")
  public QcDtos.QcResponse get(@PathVariable Long id) {
    return toResponse(qcService.get(id));
  }

  @PutMapping("/inspections/{id}")
  public QcDtos.QcResponse update(@PathVariable Long id, @Valid @RequestBody QcDtos.QcInspectionRequest request) {
    return toResponse(qcService.update(id, request));
  }

  @PostMapping("/inspections/{id}/submit")
  public QcDtos.QcResponse submit(@PathVariable Long id) {
    return toResponse(qcService.submit(id));
  }

  @PostMapping("/inspections/{id}/approve")
  public QcDtos.QcResponse approve(@PathVariable Long id) {
    return toResponse(qcService.approve(id));
  }

  @PostMapping("/inspections/{id}/reject")
  public QcDtos.QcResponse reject(@PathVariable Long id) {
    return toResponse(qcService.reject(id));
  }

  private QcDtos.QcResponse toResponse(com.manufacturing.erp.domain.QcInspection inspection) {
    List<QcDtos.QcLineResponse> lines = inspection.getLines().stream()
        .map(line -> new QcDtos.QcLineResponse(
            line.getPurchaseOrderLine() != null ? line.getPurchaseOrderLine().getId() : null,
            line.getPurchaseOrderLine() != null && line.getPurchaseOrderLine().getItem() != null
                ? line.getPurchaseOrderLine().getItem().getId()
                : null,
            line.getPurchaseOrderLine() != null && line.getPurchaseOrderLine().getItem() != null
                ? line.getPurchaseOrderLine().getItem().getName()
                : null,
            line.getPurchaseOrderLine() != null && line.getPurchaseOrderLine().getUom() != null
                ? line.getPurchaseOrderLine().getUom().getId()
                : null,
            line.getPurchaseOrderLine() != null && line.getPurchaseOrderLine().getUom() != null
                ? line.getPurchaseOrderLine().getUom().getCode()
                : null,
            line.getReceivedQty(),
            line.getAcceptedQty(),
            line.getRejectedQty(),
            line.getReason()))
        .toList();
    return new QcDtos.QcResponse(
        inspection.getId(),
        inspection.getPurchaseOrder() != null ? inspection.getPurchaseOrder().getId() : null,
        inspection.getWeighbridgeTicket() != null ? inspection.getWeighbridgeTicket().getId() : null,
        inspection.getGrn() != null ? inspection.getGrn().getId() : null,
        inspection.getStatus() != null ? inspection.getStatus().name() : null,
        inspection.getInspectionDate(),
        inspection.getSampleQty(),
        inspection.getSampleUom() != null ? inspection.getSampleUom().getId() : null,
        inspection.getSampleUom() != null ? inspection.getSampleUom().getCode() : null,
        inspection.getMethod(),
        inspection.getRemarks(),
        lines);
  }
}
