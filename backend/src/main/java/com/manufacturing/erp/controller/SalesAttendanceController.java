package com.manufacturing.erp.controller;

import com.manufacturing.erp.dto.SalesAttendanceDtos;
import com.manufacturing.erp.service.SalesAttendanceService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sales-attendance")
public class SalesAttendanceController {
  private final SalesAttendanceService salesAttendanceService;

  public SalesAttendanceController(SalesAttendanceService salesAttendanceService) {
    this.salesAttendanceService = salesAttendanceService;
  }

  @GetMapping
  public List<SalesAttendanceDtos.SalesAttendanceResponse> list(
      @RequestParam(required = false) LocalDate fromDate,
      @RequestParam(required = false) LocalDate toDate) {
    return salesAttendanceService.list(fromDate, toDate).stream()
        .map(this::toResponse)
        .toList();
  }

  @GetMapping("/{id}")
  public SalesAttendanceDtos.SalesAttendanceResponse get(@PathVariable Long id) {
    return toResponse(salesAttendanceService.get(id));
  }

  @PostMapping
  public SalesAttendanceDtos.SalesAttendanceResponse create(
      @RequestBody SalesAttendanceDtos.SalesAttendanceRequest request) {
    return toResponse(salesAttendanceService.create(request));
  }

  @PutMapping("/{id}")
  public SalesAttendanceDtos.SalesAttendanceResponse update(
      @PathVariable Long id,
      @RequestBody SalesAttendanceDtos.SalesAttendanceRequest request) {
    return toResponse(salesAttendanceService.update(id, request));
  }

  private SalesAttendanceDtos.SalesAttendanceResponse toResponse(com.manufacturing.erp.domain.SalesAttendance attendance) {
    return new SalesAttendanceDtos.SalesAttendanceResponse(
        attendance.getId(),
        attendance.getUser() != null ? attendance.getUser().getId() : null,
        attendance.getUser() != null ? attendance.getUser().getFullName() : null,
        attendance.getAttendanceDate(),
        attendance.getCheckInTime(),
        attendance.getCheckOutTime(),
        attendance.getCheckInLocation(),
        attendance.getCheckOutLocation(),
        attendance.getCheckInLat(),
        attendance.getCheckInLng(),
        attendance.getCheckOutLat(),
        attendance.getCheckOutLng(),
        attendance.getTravelKm(),
        attendance.getRatePerKm(),
        attendance.getTaAmount(),
        attendance.getDaAmount(),
        attendance.getTotalAmount(),
        attendance.getRemarks()
    );
  }
}
