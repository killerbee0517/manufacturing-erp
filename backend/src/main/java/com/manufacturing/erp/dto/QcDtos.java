package com.manufacturing.erp.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class QcDtos {
  public record QcInspectionLineRequest(
      @NotNull Long grnLineId,
      @NotNull BigDecimal receivedQty,
      @NotNull BigDecimal acceptedQty,
      @NotNull BigDecimal rejectedQty,
      String reason) {}

  public record QcInspectionRequest(
      BigDecimal sampleQty,
      Long sampleUomId,
      String method,
      String remarks,
      @NotNull List<@NotNull QcInspectionLineRequest> lines) {}

  public record QcResponse(Long id, Long grnId, String status, LocalDate inspectionDate, List<QcLineResponse> lines) {}

  public record QcLineResponse(Long grnLineId, BigDecimal receivedQty, BigDecimal acceptedQty, BigDecimal rejectedQty, String reason) {}
}
