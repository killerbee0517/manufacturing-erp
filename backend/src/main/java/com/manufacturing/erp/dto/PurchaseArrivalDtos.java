package com.manufacturing.erp.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public class PurchaseArrivalDtos {
  public record CreatePurchaseArrivalRequest(
      @NotNull Long purchaseOrderId,
      Long weighbridgeTicketId,
      @NotNull Long godownId,
      BigDecimal unloadingCharges,
      BigDecimal deductions,
      BigDecimal tdsPercent) {}

  public record PurchaseArrivalResponse(
      Long id,
      Long purchaseOrderId,
      Long weighbridgeTicketId,
      Long godownId,
      BigDecimal unloadingCharges,
      BigDecimal deductions,
      BigDecimal tdsPercent,
      BigDecimal grossAmount,
      BigDecimal netPayable,
      Instant createdAt) {}
}
