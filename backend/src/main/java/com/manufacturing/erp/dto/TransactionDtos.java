package com.manufacturing.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TransactionDtos {
  public record RfqRequest(@NotBlank String rfqNo, String status) {}
  public record RfqResponse(Long id, String rfqNo, String status) {}

  public record PurchaseOrderRequest(@NotBlank String poNo, @NotNull Long supplierId, String status) {}
  public record PurchaseOrderResponse(Long id, String poNo, Long supplierId, String status) {}

  public record SalesOrderRequest(@NotBlank String soNo, @NotNull Long customerId, String status) {}
  public record SalesOrderResponse(Long id, String soNo, Long customerId, String status) {}

  public record DeliveryRequest(@NotBlank String deliveryNo, @NotNull Long salesOrderId) {}
  public record DeliveryResponse(Long id, String deliveryNo, Long salesOrderId) {}

  public record DebitNoteRequest(@NotBlank String debitNoteNo, @NotNull Long supplierId, @NotBlank String reason) {}
  public record DebitNoteResponse(Long id, String debitNoteNo, Long supplierId, String reason) {}
}
