package com.manufacturing.erp.controller;

import com.manufacturing.erp.dto.QcDtos;
import com.manufacturing.erp.repository.QcInspectionRepository;
import com.manufacturing.erp.service.QcService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/qc")
public class QcController {
  private final QcService qcService;
  private final QcInspectionRepository qcInspectionRepository;

  public QcController(QcService qcService, QcInspectionRepository qcInspectionRepository) {
    this.qcService = qcService;
    this.qcInspectionRepository = qcInspectionRepository;
  }

  @GetMapping("/inspections")
  public List<QcDtos.QcResponse> list() {
    return qcInspectionRepository.findAll().stream()
        .map(inspection -> new QcDtos.QcResponse(inspection.getId(), inspection.getStatus().name()))
        .toList();
  }

  @PostMapping("/inspections")
  public QcDtos.QcResponse update(@Valid @RequestBody QcDtos.QcUpdateRequest request) {
    var inspection = qcService.updateStatus(request);
    return new QcDtos.QcResponse(inspection.getId(), inspection.getStatus().name());
  }
}
