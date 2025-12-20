package com.manufacturing.erp.controller;

import com.manufacturing.erp.dto.QcDtos;
import com.manufacturing.erp.service.QcService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/qc")
public class QcController {
  private final QcService qcService;

  public QcController(QcService qcService) {
    this.qcService = qcService;
  }

  @PostMapping("/inspections")
  public QcDtos.QcResponse update(@Valid @RequestBody QcDtos.QcUpdateRequest request) {
    var inspection = qcService.updateStatus(request);
    return new QcDtos.QcResponse(inspection.getId(), inspection.getStatus().name());
  }
}
