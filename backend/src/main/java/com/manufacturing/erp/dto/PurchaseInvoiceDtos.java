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
      @NotNull BigDecimal quantity,
      @NotNull BigDecimal lineAmount) {}

  public record CreateInvoiceRequest(
      @NotBlank String invoiceNo,
      @NotNull Long supplierId,
      @NotNull LocalDate invoiceDate,
      @NotEmpty List<LineRequest> lines) {}

  public record InvoiceResponse(Long id, String invoiceNo, BigDecimal totalAmount, BigDecimal tdsAmount, BigDecimal netPayable) {}
}
