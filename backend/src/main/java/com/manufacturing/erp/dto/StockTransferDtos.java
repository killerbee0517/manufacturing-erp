package com.manufacturing.erp.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class StockTransferDtos {
  public record TransferRequest(
      @NotNull Long itemId,
      @NotNull Long fromLocationId,
      @NotNull Long toLocationId,
      @NotNull BigDecimal quantity,
      @NotNull BigDecimal weight) {}
}
