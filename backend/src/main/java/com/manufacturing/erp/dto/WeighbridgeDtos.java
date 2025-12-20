package com.manufacturing.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class WeighbridgeDtos {
  public record ReadingRequest(
      @NotBlank String readingType,
      @NotNull BigDecimal weight,
      @NotNull Instant readingTime) {}

  public record CreateTicketRequest(
      @NotBlank String ticketNo,
      @NotBlank String vehicleNo,
      @NotNull Long supplierId,
      @NotNull Long itemId,
      @NotNull LocalDate dateIn,
      @NotNull LocalTime timeIn,
      @NotEmpty List<ReadingRequest> readings) {}

  public record TicketResponse(Long id, String ticketNo, BigDecimal gross, BigDecimal tare, BigDecimal net) {}
}
