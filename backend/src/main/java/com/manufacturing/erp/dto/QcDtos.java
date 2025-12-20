package com.manufacturing.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class QcDtos {
  public record QcUpdateRequest(
      @NotNull Long grnLineId,
      @NotBlank String status,
      @NotNull LocalDate inspectionDate) {}

  public record QcResponse(Long id, String status) {}
}
