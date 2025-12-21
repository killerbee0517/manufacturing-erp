package com.manufacturing.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TransactionDtos {
  public record RfqLineRequest(
      Long id,
      @NotNull Long itemId,
      @NotNull Long uomId,
      @NotNull @Positive BigDecimal quantity,
      BigDecimal rateExpected,
      String remarks) {}

  public record RfqLineResponse(
      Long id,
      Long itemId,
      Long uomId,
      BigDecimal quantity,
      BigDecimal rateExpected,
      String remarks) {}

  public record RfqRequest(
      @NotBlank String rfqNo,
      @NotNull Long supplierId,
      LocalDate rfqDate,
      String remarks,
      @NotEmpty List<RfqLineRequest> lines) {}

  public record RfqResponse(
      Long id,
      String rfqNo,
      Long supplierId,
      LocalDate rfqDate,
      String remarks,
      String status,
      List<RfqLineResponse> lines) {}

  public record PurchaseOrderLineRequest(
      Long id,
      @NotNull Long itemId,
      @NotNull Long uomId,
      @NotNull @Positive BigDecimal quantity,
      @NotNull @Positive BigDecimal rate,
      BigDecimal amount,
      String remarks) {}

  public record PurchaseOrderLineResponse(
      Long id,
      Long itemId,
      Long uomId,
      BigDecimal quantity,
      BigDecimal rate,
      BigDecimal amount,
      String remarks) {}

  public record PurchaseOrderRequest(
      @NotBlank String poNo,
      @NotNull Long supplierId,
      LocalDate poDate,
      String remarks,
      @NotEmpty List<PurchaseOrderLineRequest> lines) {}

  public record PurchaseOrderResponse(
      Long id,
      String poNo,
      Long supplierId,
      LocalDate poDate,
      String remarks,
      BigDecimal totalAmount,
      String status,
      List<PurchaseOrderLineResponse> lines) {}

  public record SalesOrderRequest(@NotBlank String soNo, @NotNull Long customerId, String status) {}
  public record SalesOrderResponse(Long id, String soNo, Long customerId, String status) {}

  public record DeliveryRequest(@NotBlank String deliveryNo, @NotNull Long salesOrderId) {}
  public record DeliveryResponse(Long id, String deliveryNo, Long salesOrderId) {}

  public record DebitNoteRequest(@NotBlank String debitNoteNo, @NotNull Long supplierId, @NotBlank String reason) {}
  public record DebitNoteResponse(Long id, String debitNoteNo, Long supplierId, String reason) {}
}
