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
      String itemName,
      Long uomId,
      String uomCode,
      BigDecimal expectedQty,
      BigDecimal receivedQty,
      BigDecimal acceptedQty,
      BigDecimal rejectedQty,
      BigDecimal weight,
      BigDecimal rate,
      BigDecimal amount) {}

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

  public record UpdateGrnRequest(
      Long godownId,
      String narration) {}

  public record ConfirmGrnRequest(
      @NotNull Long godownId,
      String narration,
      @NotNull List<GrnLineRequest> lines) {}

  public record GrnResponse(
      Long id,
      String grnNo,
      Long supplierId,
      String supplierName,
      Long purchaseOrderId,
      String purchaseOrderNo,
      Long weighbridgeTicketId,
      String weighbridgeSerialNo,
      Long godownId,
      String godownName,
      LocalDate grnDate,
      BigDecimal firstWeight,
      BigDecimal secondWeight,
      BigDecimal netWeight,
      String narration,
      String status,
      List<GrnLineResponse> lines) {}
}
