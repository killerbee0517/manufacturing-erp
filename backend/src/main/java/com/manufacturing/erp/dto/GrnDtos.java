package com.manufacturing.erp.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class GrnDtos {
  public record CreateGrnRequest(
      String grnNo,
      @NotNull Long supplierId,
      Long purchaseOrderId,
      Long weighbridgeTicketId,
      @NotNull LocalDate grnDate,
      @NotNull Long itemId,
      @NotNull Long uomId,
      @NotNull BigDecimal quantity,
      BigDecimal firstWeight,
      BigDecimal secondWeight,
      BigDecimal netWeight,
      String narration) {}

  public record GrnResponse(
      Long id,
      String grnNo,
      Long supplierId,
      Long purchaseOrderId,
      Long weighbridgeTicketId,
      LocalDate grnDate,
      Long itemId,
      Long uomId,
      BigDecimal quantity,
      BigDecimal firstWeight,
      BigDecimal secondWeight,
      BigDecimal netWeight,
      String narration,
      String status) {}
}
