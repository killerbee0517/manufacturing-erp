package com.manufacturing.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class SalesInvoiceDtos {
  public record CreateSalesInvoiceRequest(
      @NotBlank String invoiceNo,
      @NotNull Long customerId,
      Long brokerId,
      @NotNull LocalDate invoiceDate,
      @NotNull BigDecimal totalAmount) {}
}
