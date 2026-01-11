package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Company;
import com.manufacturing.erp.domain.SalesAttendance;
import com.manufacturing.erp.domain.User;
import com.manufacturing.erp.dto.SalesAttendanceDtos;
import com.manufacturing.erp.repository.CompanyRepository;
import com.manufacturing.erp.repository.SalesAttendanceRepository;
import com.manufacturing.erp.repository.UserRepository;
import com.manufacturing.erp.security.CompanyContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SalesAttendanceService {
  private final SalesAttendanceRepository salesAttendanceRepository;
  private final UserRepository userRepository;
  private final CompanyRepository companyRepository;
  private final CompanyContext companyContext;

  public SalesAttendanceService(SalesAttendanceRepository salesAttendanceRepository,
                                UserRepository userRepository,
                                CompanyRepository companyRepository,
                                CompanyContext companyContext) {
    this.salesAttendanceRepository = salesAttendanceRepository;
    this.userRepository = userRepository;
    this.companyRepository = companyRepository;
    this.companyContext = companyContext;
  }

  public List<SalesAttendance> list(LocalDate fromDate, LocalDate toDate) {
    Company company = requireCompany();
    return salesAttendanceRepository.search(company.getId(), fromDate, toDate);
  }

  public SalesAttendance get(Long id) {
    Company company = requireCompany();
    return salesAttendanceRepository.findByIdAndCompanyId(id, company.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Attendance not found"));
  }

  @Transactional
  public SalesAttendance create(SalesAttendanceDtos.SalesAttendanceRequest request) {
    Company company = requireCompany();
    User user = requireUser();
    SalesAttendance attendance = new SalesAttendance();
    attendance.setCompany(company);
    attendance.setUser(user);
    applyRequest(attendance, request);
    return salesAttendanceRepository.save(attendance);
  }

  @Transactional
  public SalesAttendance update(Long id, SalesAttendanceDtos.SalesAttendanceRequest request) {
    SalesAttendance attendance = get(id);
    applyRequest(attendance, request);
    return salesAttendanceRepository.save(attendance);
  }

  private void applyRequest(SalesAttendance attendance, SalesAttendanceDtos.SalesAttendanceRequest request) {
    if (request == null) {
      return;
    }
    attendance.setAttendanceDate(request.attendanceDate());
    attendance.setCheckInTime(request.checkInTime());
    attendance.setCheckOutTime(request.checkOutTime());
    attendance.setCheckInLocation(request.checkInLocation());
    attendance.setCheckOutLocation(request.checkOutLocation());
    attendance.setCheckInLat(request.checkInLat());
    attendance.setCheckInLng(request.checkInLng());
    attendance.setCheckOutLat(request.checkOutLat());
    attendance.setCheckOutLng(request.checkOutLng());
    attendance.setTravelKm(request.travelKm());
    attendance.setRatePerKm(request.ratePerKm());
    BigDecimal taAmount = computeTaAmount(request.travelKm(), request.ratePerKm());
    BigDecimal daAmount = request.daAmount() != null ? request.daAmount() : BigDecimal.ZERO;
    attendance.setTaAmount(taAmount);
    attendance.setDaAmount(daAmount);
    attendance.setTotalAmount(taAmount.add(daAmount));
    attendance.setRemarks(request.remarks());
  }

  private BigDecimal computeTaAmount(BigDecimal travelKm, BigDecimal ratePerKm) {
    if (travelKm == null || ratePerKm == null) {
      return BigDecimal.ZERO;
    }
    return travelKm.multiply(ratePerKm).setScale(2, RoundingMode.HALF_UP);
  }

  private User requireUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing user context");
    }
    String username = authentication.getName();
    return userRepository.findByUsername(username)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
  }

  private Company requireCompany() {
    Long companyId = companyContext.getCompanyId();
    if (companyId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing company context");
    }
    return companyRepository.findById(companyId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
  }
}
