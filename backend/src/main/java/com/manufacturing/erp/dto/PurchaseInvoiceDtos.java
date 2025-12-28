package com.manufacturing.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PurchaseInvoiceDtos {
  public record LineRequest(
      @NotNull Long itemId,
      @NotNull Long uomId,
      @NotNull BigDecimal quantity,
      @NotNull BigDecimal rate,
      BigDecimal amount) {}

  public record UpdateInvoiceRequest(
      String supplierInvoiceNo,
      @NotNull LocalDate invoiceDate,
      String narration) {}

  public record InvoiceLineResponse(
      Long id,
      Long itemId,
      String itemName,
      Long uomId,
      String uomCode,
      BigDecimal quantity,
      BigDecimal rate,
      BigDecimal amount) {}

  public record InvoiceResponse(
      Long id,
      String invoiceNo,
      Long supplierId,
      String supplierName,
      Long purchaseOrderId,
      String purchaseOrderNo,
      Long grnId,
      String grnNo,
      String supplierInvoiceNo,
      LocalDate invoiceDate,
      String narration,
      String status,
      BigDecimal subtotal,
      BigDecimal taxTotal,
      BigDecimal roundOff,
      BigDecimal grandTotal,
      List<InvoiceLineResponse> lines) {}
}
