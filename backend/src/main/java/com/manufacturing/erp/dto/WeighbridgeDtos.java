package com.manufacturing.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class WeighbridgeDtos {
  public record CreateTicketRequest(
      String ticketNo,
      @NotBlank String vehicleNo,
      @NotNull Long supplierId,
      @NotNull Long itemId,
      @NotNull LocalDate dateIn,
      @NotNull LocalTime timeIn,
      @NotNull BigDecimal grossWeight,
      @NotNull BigDecimal unloadedWeight,
      LocalDate dateOut,
      LocalTime timeOut) {}

  public record TicketResponse(
      Long id,
      String ticketNo,
      String vehicleNo,
      Long supplierId,
      Long itemId,
      LocalDate dateIn,
      LocalTime timeIn,
      LocalDate dateOut,
      LocalTime timeOut,
      BigDecimal grossWeight,
      BigDecimal unloadedWeight,
      BigDecimal netWeight) {}
}
