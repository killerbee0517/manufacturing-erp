package com.manufacturing.erp.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class DebitNoteDtos {
  public record LineRequest(
      Long id,
      Long ruleId,
      String description,
      BigDecimal baseValue,
      BigDecimal rate,
      BigDecimal amount,
      String remarks) {}

  public record UpdateDebitNoteRequest(
      LocalDate dnDate,
      String narration,
      List<LineRequest> lines) {}

  public record DebitNoteLineResponse(
      Long id,
      Long ruleId,
      String description,
      BigDecimal baseValue,
      BigDecimal rate,
      BigDecimal amount,
      String remarks) {}

  public record DebitNoteResponse(
      Long id,
      String dnNo,
      Long supplierId,
      String supplierName,
      Long purchaseInvoiceId,
      String purchaseInvoiceNo,
      Long purchaseOrderId,
      String purchaseOrderNo,
      Long grnId,
      String grnNo,
      LocalDate dnDate,
      String narration,
      String status,
      BigDecimal totalDeduction,
      List<DebitNoteLineResponse> lines) {}
}
