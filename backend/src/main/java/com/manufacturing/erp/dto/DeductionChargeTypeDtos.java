package com.manufacturing.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class DeductionChargeTypeDtos {
  public record DeductionChargeTypeRequest(
      Long id,
      @NotBlank String code,
      @NotBlank String name,
      @NotBlank String defaultCalcType,
      BigDecimal defaultRate,
      @NotNull Boolean isDeduction,
      Boolean enabled) {}

  public record DeductionChargeTypeResponse(
      Long id,
      String code,
      String name,
      String defaultCalcType,
      BigDecimal defaultRate,
      Boolean isDeduction,
      Boolean enabled) {}
}
