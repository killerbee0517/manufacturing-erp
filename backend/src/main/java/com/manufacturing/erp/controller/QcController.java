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
  public List<QcDtos.QcResponse> list(@RequestParam(required = false) Long grnId) {
    return qcService.list(grnId).stream().map(this::toResponse).toList();
  }

  @PostMapping("/inspections/from-grn/{grnId}")
  public QcDtos.QcResponse createFromGrn(@PathVariable Long grnId) {
    return toResponse(qcService.createDraftFromGrn(grnId));
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
            line.getGrnLine() != null ? line.getGrnLine().getId() : null,
            line.getReceivedQty(),
            line.getAcceptedQty(),
            line.getRejectedQty(),
            line.getReason()))
        .toList();
    return new QcDtos.QcResponse(
        inspection.getId(),
        inspection.getGrn() != null ? inspection.getGrn().getId() : null,
        inspection.getStatus() != null ? inspection.getStatus().name() : null,
        inspection.getInspectionDate(),
        lines);
  }
}
