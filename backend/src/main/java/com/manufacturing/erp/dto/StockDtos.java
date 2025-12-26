package com.manufacturing.erp.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class StockDtos {
  public record StockOnHandResponse(
      Long itemId,
      Long godownId,
      BigDecimal qtyIn,
      BigDecimal qtyOut,
      BigDecimal balance) {}

  public record StockLedgerFilter(Long godownId, Long itemId, LocalDate from, LocalDate to) {}

  public record StockLedgerResponse(
      Long id,
      String docType,
      Long docId,
      Long docLineId,
      Long itemId,
      Long godownId,
      Long fromGodownId,
      Long toGodownId,
      BigDecimal qtyIn,
      BigDecimal qtyOut,
      BigDecimal rate,
      BigDecimal amount,
      String status,
      String txnType,
      Instant postedAt) {}

  public record SalesInvoiceLineRequest(
      @NotNull Long itemId,
      @NotNull Long uomId,
      @NotNull BigDecimal quantity,
      Long godownId,
      BigDecimal rate,
      BigDecimal amount) {}

  public record SalesInvoiceRequest(
      String invoiceNo,
      @NotNull Long customerId,
      Long brokerId,
      @NotNull LocalDate invoiceDate,
      @NotNull BigDecimal totalAmount,
      List<SalesInvoiceLineRequest> lines) {}
}
