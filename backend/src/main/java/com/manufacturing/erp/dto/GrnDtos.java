package com.manufacturing.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class GrnDtos {
  public record GrnLineRequest(
      @NotNull Long itemId,
      @NotBlank String bagType,
      @NotNull Integer bagCount,
      @NotNull BigDecimal quantity,
      @NotNull BigDecimal weight) {}

  public record CreateGrnRequest(
      @NotBlank String grnNo,
      @NotNull Long supplierId,
      @NotNull Long weighbridgeTicketId,
      @NotNull LocalDate grnDate,
      @NotEmpty List<GrnLineRequest> lines) {}

  public record GrnResponse(Long id, String grnNo, String status) {}
}
