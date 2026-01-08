package com.manufacturing.erp.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

public class PurchaseArrivalDtos {
  public record CreatePurchaseArrivalRequest(
      @NotNull Long purchaseOrderId,
      Long weighbridgeTicketId,
      Long brokerId,
      BigDecimal brokerageAmount,
      @NotNull Long godownId,
      BigDecimal unloadingCharges,
      BigDecimal deductions,
      BigDecimal tdsPercent,
      java.util.List<PurchaseArrivalChargeRequest> charges) {}

  public record PurchaseArrivalChargeRequest(
      @NotNull Long chargeTypeId,
      String calcType,
      BigDecimal rate,
      BigDecimal amount,
      Boolean isDeduction,
      @NotNull String payablePartyType,
      Long payablePartyId,
      String remarks) {}

  public record PurchaseArrivalResponse(
      Long id,
      Long purchaseOrderId,
      Long weighbridgeTicketId,
      Long brokerId,
      String brokerName,
      BigDecimal brokerageAmount,
      Long godownId,
      BigDecimal unloadingCharges,
      BigDecimal deductions,
      BigDecimal tdsPercent,
      BigDecimal grossAmount,
      BigDecimal netPayable,
      java.util.List<PurchaseArrivalChargeResponse> charges,
      Instant createdAt) {}

  public record PurchaseArrivalChargeResponse(
      Long id,
      Long chargeTypeId,
      String calcType,
      BigDecimal rate,
      BigDecimal amount,
      Boolean isDeduction,
      String payablePartyType,
      Long payablePartyId,
      String remarks) {}
}
