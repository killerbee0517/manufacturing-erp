package com.manufacturing.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ExpensePartyDtos {
  public record ExpensePartyRequest(
      Long id,
      @NotBlank String name,
      @NotBlank String partyType) {}

  public record ExpensePartyResponse(
      Long id,
      String name,
      String partyType,
      Long ledgerId) {}
}
