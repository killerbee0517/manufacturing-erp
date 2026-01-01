package com.manufacturing.erp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TransactionDtos {
  public record RfqLineRequest(
      Long id,
      @NotNull Long itemId,
      @NotNull Long uomId,
      Long brokerId,
      @NotNull @Positive BigDecimal quantity,
      @PositiveOrZero BigDecimal rateExpected,
      String remarks) {}

  public record RfqLineResponse(
      Long id,
      Long itemId,
      Long uomId,
      Long brokerId,
      BigDecimal quantity,
      BigDecimal rateExpected,
      String remarks) {}

  public record RfqSupplierInvite(Long supplierId, String status, String remarks) {}

  public record RfqAwardLine(
      Long rfqLineId,
      Long supplierId,
      BigDecimal awardQty,
      BigDecimal awardRate,
      LocalDate deliveryDate,
      String status) {}

  public record RfqQuoteLineRequest(
      @NotNull Long rfqLineId,
      @PositiveOrZero BigDecimal quotedQty,
      @PositiveOrZero BigDecimal quotedRate,
      LocalDate deliveryDate,
      String remarks) {}

  public record RfqQuoteLineResponse(
      Long rfqLineId,
      BigDecimal quotedQty,
      BigDecimal quotedRate,
      LocalDate deliveryDate,
      String remarks) {}

  public record RfqQuoteSaveRequest(String paymentTermsOverride, String remarks, @NotEmpty List<@Valid RfqQuoteLineRequest> lines) {}

  public record RfqAwardAllocation(@NotNull Long rfqLineId, @NotNull @Positive BigDecimal awardQty, BigDecimal awardRate, LocalDate deliveryDate) {}

  public record RfqSupplierAward(@NotNull Long supplierId, @NotEmpty List<@Valid RfqAwardAllocation> allocations) {}

  public record RfqQuoteResponse(
      Long supplierId,
      String supplierName,
      String status,
      String paymentTermsOverride,
      String remarks,
      java.time.Instant submittedAt,
      List<RfqQuoteLineResponse> lines) {}

  public record RfqQuoteSupplierSummary(
      Long supplierId,
      String supplierName,
      String status,
      BigDecimal totalAmount,
      BigDecimal totalQty,
      java.time.Instant submittedAt) {}

  public record RfqRequest(
      String rfqNo,
      LocalDate rfqDate,
      String paymentTerms,
      String narration,
      @NotEmpty List<Long> supplierIds,
      @NotEmpty @Valid List<RfqLineRequest> lines) {}

  public record RfqPoSummary(Long supplierId, Long poId, String poNo) {}

  public record RfqResponse(
      Long id,
      String rfqNo,
      List<RfqSupplierInvite> suppliers,
      LocalDate rfqDate,
      String paymentTerms,
      String narration,
      String closureReason,
      String status,
      List<RfqLineResponse> lines,
      List<RfqAwardLine> awards,
      List<RfqQuoteSupplierSummary> quoteSummaries,
      java.util.Map<Long, java.util.List<Long>> poIdsBySupplier,
      java.util.List<RfqPoSummary> purchaseOrders) {}

  public record RfqCloseRequest(@NotBlank String closureReason) {}

  public record RfqCloseResponse(Long id, String status, String closureReason, Long purchaseOrderId) {}
  public record RfqAwardRequest(@Valid List<RfqSupplierAward> supplierAwards,
                                @Valid List<RfqAwardLine> awards,
                                String remarks,
                                Boolean closeRemaining,
                                String closureReason) {}

  public record PurchaseOrderLineRequest(
      Long id,
      @NotNull Long itemId,
      @NotNull Long uomId,
      @NotNull @Positive BigDecimal quantity,
      @NotNull @PositiveOrZero BigDecimal rate,
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
      String poNo,
      Long rfqId,
      @NotNull Long supplierId,
      LocalDate poDate,
      LocalDate deliveryDate,
      String supplierInvoiceNo,
      String purchaseLedger,
      BigDecimal currentLedgerBalance,
      String narration,
      @NotEmpty List<PurchaseOrderLineRequest> lines) {}

  public record PurchaseOrderResponse(
      Long id,
      String poNo,
      Long rfqId,
      Long supplierId,
      String supplierName,
      LocalDate poDate,
      LocalDate deliveryDate,
      String supplierInvoiceNo,
      String purchaseLedger,
      BigDecimal currentLedgerBalance,
      String narration,
      BigDecimal totalAmount,
      String status,
      List<PurchaseOrderLineResponse> lines) {}

  public record SalesOrderLineRequest(
      Long id,
      @NotNull Long itemId,
      @NotNull Long uomId,
      @NotNull @Positive BigDecimal quantity,
      @NotNull @PositiveOrZero BigDecimal rate) {}

  public record SalesOrderRequest(
      Long id,
      String soNo,
      @NotNull Long customerId,
      LocalDate orderDate,
      String status,
      String narration,
      @NotEmpty List<@Valid SalesOrderLineRequest> lines) {}

  public record SalesOrderResponse(
      Long id,
      String soNo,
      Long customerId,
      LocalDate orderDate,
      String status,
      String narration,
      List<SalesOrderLineResponse> lines) {}

  public record SalesOrderLineResponse(Long id, Long itemId, Long uomId, BigDecimal quantity, BigDecimal rate) {}

  public record DeliveryRequest(@NotBlank String deliveryNo, @NotNull Long salesOrderId) {}
  public record DeliveryResponse(Long id, String deliveryNo, Long salesOrderId) {}

  public record DebitNoteRequest(@NotBlank String debitNoteNo, @NotNull Long supplierId, @NotBlank String reason) {}
  public record DebitNoteResponse(Long id, String debitNoteNo, Long supplierId, String reason) {}
}
