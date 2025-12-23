package com.manufacturing.erp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class LedgerDtos {
  public record LedgerResponse(
      Long id,
      String name,
      String type,
      BigDecimal balance) {}

  public record LedgerBalanceResponse(Long ledgerId, BigDecimal balance) {}

  public record LedgerStatementEntry(
      Long voucherId,
      String voucherNo,
      LocalDate voucherDate,
      String narration,
      String referenceType,
      Long referenceId,
      BigDecimal drAmount,
      BigDecimal crAmount,
      BigDecimal runningBalance) {}
}
