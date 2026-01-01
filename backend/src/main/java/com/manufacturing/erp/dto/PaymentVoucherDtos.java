package com.manufacturing.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class PaymentVoucherDtos {
  public record AllocationRequest(
      Long id,
      Long purchaseInvoiceId,
      BigDecimal allocatedAmount,
      String remarks) {}

  public record PaymentVoucherRequest(
      @NotNull LocalDate voucherDate,
      @NotBlank String partyType,
      @NotNull Long partyId,
      @NotBlank String paymentDirection,
      @NotBlank String paymentMode,
      Long bankId,
      @NotNull BigDecimal amount,
      String narration,
      String chequeNumber,
      LocalDate chequeDate,
      List<AllocationRequest> allocations) {}

  public record AllocationResponse(
      Long id,
      Long purchaseInvoiceId,
      String purchaseInvoiceNo,
      BigDecimal allocatedAmount,
      String remarks) {}

  public record PaymentVoucherResponse(
      Long id,
      String voucherNo,
      LocalDate voucherDate,
      String partyType,
      Long partyId,
      String partyName,
      String paymentDirection,
      String paymentMode,
      Long bankId,
      String bankName,
      BigDecimal amount,
      String narration,
      String status,
      String chequeNumber,
      LocalDate chequeDate,
      List<AllocationResponse> allocations) {}
}
