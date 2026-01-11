package com.manufacturing.erp.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class SalesAttendanceDtos {
  public record SalesAttendanceRequest(
      LocalDate attendanceDate,
      LocalTime checkInTime,
      LocalTime checkOutTime,
      String checkInLocation,
      String checkOutLocation,
      Double checkInLat,
      Double checkInLng,
      Double checkOutLat,
      Double checkOutLng,
      BigDecimal travelKm,
      BigDecimal ratePerKm,
      BigDecimal daAmount,
      String remarks) {}

  public record SalesAttendanceResponse(
      Long id,
      Long userId,
      String userName,
      LocalDate attendanceDate,
      LocalTime checkInTime,
      LocalTime checkOutTime,
      String checkInLocation,
      String checkOutLocation,
      Double checkInLat,
      Double checkInLng,
      Double checkOutLat,
      Double checkOutLng,
      BigDecimal travelKm,
      BigDecimal ratePerKm,
      BigDecimal taAmount,
      BigDecimal daAmount,
      BigDecimal totalAmount,
      String remarks) {}
}
