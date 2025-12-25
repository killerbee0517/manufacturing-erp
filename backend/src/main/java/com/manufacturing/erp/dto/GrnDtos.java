package com.manufacturing.erp.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class GrnDtos {
  public record GrnLineRequest(
      @NotNull Long itemId,
      @NotNull Long uomId,
      @NotNull BigDecimal quantity,
      BigDecimal weight,
      Long poLineId,
      BigDecimal rate,
      BigDecimal amount) {}

  public record GrnLineResponse(
      Long id,
      Long itemId,
      Long uomId,
      BigDecimal quantity,
      BigDecimal weight) {}

  public record CreateGrnRequest(
      String grnNo,
      @NotNull Long purchaseOrderId,
      Long weighbridgeTicketId,
      Long godownId,
      @NotNull LocalDate grnDate,
      BigDecimal firstWeight,
      BigDecimal secondWeight,
      BigDecimal netWeight,
      String narration,
      @NotNull List<GrnLineRequest> lines) {}

  public record ConfirmGrnRequest(
      @NotNull Long godownId,
      String narration,
      @NotNull List<GrnLineRequest> lines) {}

  public record GrnResponse(
      Long id,
      String grnNo,
      Long supplierId,
      Long purchaseOrderId,
      Long weighbridgeTicketId,
      Long godownId,
      LocalDate grnDate,
      BigDecimal firstWeight,
      BigDecimal secondWeight,
      BigDecimal netWeight,
      String narration,
      String status,
      List<GrnLineResponse> lines) {}
}
