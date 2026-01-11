package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "sales_attendance")
public class SalesAttendance extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "attendance_date", nullable = false)
  private LocalDate attendanceDate;

  @Column(name = "check_in_time")
  private LocalTime checkInTime;

  @Column(name = "check_out_time")
  private LocalTime checkOutTime;

  @Column(name = "check_in_location")
  private String checkInLocation;

  @Column(name = "check_out_location")
  private String checkOutLocation;

  @Column(name = "check_in_lat")
  private Double checkInLat;

  @Column(name = "check_in_lng")
  private Double checkInLng;

  @Column(name = "check_out_lat")
  private Double checkOutLat;

  @Column(name = "check_out_lng")
  private Double checkOutLng;

  @Column(name = "travel_km")
  private BigDecimal travelKm;

  @Column(name = "rate_per_km")
  private BigDecimal ratePerKm;

  @Column(name = "ta_amount")
  private BigDecimal taAmount;

  @Column(name = "da_amount")
  private BigDecimal daAmount;

  @Column(name = "total_amount")
  private BigDecimal totalAmount;

  @Column
  private String remarks;

  public Company getCompany() {
    return company;
  }

  public void setCompany(Company company) {
    this.company = company;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public LocalDate getAttendanceDate() {
    return attendanceDate;
  }

  public void setAttendanceDate(LocalDate attendanceDate) {
    this.attendanceDate = attendanceDate;
  }

  public LocalTime getCheckInTime() {
    return checkInTime;
  }

  public void setCheckInTime(LocalTime checkInTime) {
    this.checkInTime = checkInTime;
  }

  public LocalTime getCheckOutTime() {
    return checkOutTime;
  }

  public void setCheckOutTime(LocalTime checkOutTime) {
    this.checkOutTime = checkOutTime;
  }

  public String getCheckInLocation() {
    return checkInLocation;
  }

  public void setCheckInLocation(String checkInLocation) {
    this.checkInLocation = checkInLocation;
  }

  public String getCheckOutLocation() {
    return checkOutLocation;
  }

  public void setCheckOutLocation(String checkOutLocation) {
    this.checkOutLocation = checkOutLocation;
  }

  public Double getCheckInLat() {
    return checkInLat;
  }

  public void setCheckInLat(Double checkInLat) {
    this.checkInLat = checkInLat;
  }

  public Double getCheckInLng() {
    return checkInLng;
  }

  public void setCheckInLng(Double checkInLng) {
    this.checkInLng = checkInLng;
  }

  public Double getCheckOutLat() {
    return checkOutLat;
  }

  public void setCheckOutLat(Double checkOutLat) {
    this.checkOutLat = checkOutLat;
  }

  public Double getCheckOutLng() {
    return checkOutLng;
  }

  public void setCheckOutLng(Double checkOutLng) {
    this.checkOutLng = checkOutLng;
  }

  public BigDecimal getTravelKm() {
    return travelKm;
  }

  public void setTravelKm(BigDecimal travelKm) {
    this.travelKm = travelKm;
  }

  public BigDecimal getRatePerKm() {
    return ratePerKm;
  }

  public void setRatePerKm(BigDecimal ratePerKm) {
    this.ratePerKm = ratePerKm;
  }

  public BigDecimal getTaAmount() {
    return taAmount;
  }

  public void setTaAmount(BigDecimal taAmount) {
    this.taAmount = taAmount;
  }

  public BigDecimal getDaAmount() {
    return daAmount;
  }

  public void setDaAmount(BigDecimal daAmount) {
    this.daAmount = daAmount;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(BigDecimal totalAmount) {
    this.totalAmount = totalAmount;
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }
}
