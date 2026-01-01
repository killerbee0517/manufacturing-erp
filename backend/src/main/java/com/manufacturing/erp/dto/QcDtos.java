package com.manufacturing.erp.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class QcDtos {
  public record QcInspectionLineRequest(
      @NotNull Long poLineId,
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

  public record QcResponse(
      Long id,
      Long purchaseOrderId,
      Long weighbridgeTicketId,
      Long grnId,
      String status,
      LocalDate inspectionDate,
      BigDecimal sampleQty,
      Long sampleUomId,
      String sampleUomCode,
      String method,
      String remarks,
      List<QcLineResponse> lines) {}

  public record QcLineResponse(
      Long poLineId,
      Long itemId,
      String itemName,
      Long uomId,
      String uomCode,
      BigDecimal receivedQty,
      BigDecimal acceptedQty,
      BigDecimal rejectedQty,
      String reason) {}
}
