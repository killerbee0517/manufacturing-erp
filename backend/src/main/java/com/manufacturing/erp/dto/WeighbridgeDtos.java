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
      Long supplierId,
      Long itemId,
      @NotNull LocalDate dateIn,
      @NotNull LocalTime timeIn,
      @NotNull BigDecimal grossWeight,
      BigDecimal unloadedWeight,
      LocalDate secondDate,
      LocalTime secondTime) {}

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
