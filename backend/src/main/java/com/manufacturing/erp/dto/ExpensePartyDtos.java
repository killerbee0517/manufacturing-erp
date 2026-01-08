package com.manufacturing.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ExpensePartyDtos {
  public record ExpensePartyRequest(
      Long id,
      Long partyId,
      @NotBlank String name,
      @NotBlank String partyType) {}

  public record ExpensePartyResponse(
      Long id,
      Long partyId,
      String name,
      String partyType,
      Long ledgerId) {}
}
