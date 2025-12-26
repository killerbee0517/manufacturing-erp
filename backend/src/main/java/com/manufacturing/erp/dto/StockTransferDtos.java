package com.manufacturing.erp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class StockTransferDtos {
  public record StockTransferLineRequest(
      @NotNull Long itemId,
      @NotNull Long uomId,
      @NotNull @Positive BigDecimal qty) {}

  public record StockTransferRequest(
      Long id,
      String transferNo,
      @NotNull Long fromGodownId,
      @NotNull Long toGodownId,
      @NotNull LocalDate transferDate,
      String narration,
      @NotEmpty @Valid List<StockTransferLineRequest> lines) {}

  public record StockTransferResponse(
      Long id,
      String transferNo,
      Long fromGodownId,
      Long toGodownId,
      LocalDate transferDate,
      String status,
      String narration,
      List<StockTransferLineResponse> lines) {}

  public record StockTransferLineResponse(
      Long id,
      Long itemId,
      Long uomId,
      BigDecimal qty) {}

  public record StockTransferPostRequest(@NotNull Long id) {}
}
