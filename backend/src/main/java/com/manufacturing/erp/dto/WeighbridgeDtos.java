package com.manufacturing.erp.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class WeighbridgeDtos {
  public record CreateTicketRequest(
      String serialNo,
      @NotNull Long poId,
      @NotNull Long vehicleId,
      @NotNull LocalDate dateIn,
      @NotNull LocalTime timeIn,
      @NotNull BigDecimal grossWeight) {}

  public record UnloadTicketRequest(
      @NotNull Long poId,
      @NotNull Long vehicleId,
      LocalDate secondDate,
      LocalTime secondTime,
      @NotNull BigDecimal unloadedWeight) {}

  public record TicketResponse(
      Long id,
      String serialNo,
      Long vehicleId,
      Long poId,
      Long supplierId,
      Long itemId,
      LocalDate dateIn,
      LocalTime timeIn,
      LocalDate secondDate,
      LocalTime secondTime,
      BigDecimal grossWeight,
      BigDecimal unloadedWeight,
      BigDecimal netWeight,
      String status) {}
}
