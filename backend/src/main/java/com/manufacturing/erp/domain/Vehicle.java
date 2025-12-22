package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "vehicles")
public class Vehicle extends BaseEntity {
  @Column(name = "vehicle_no", nullable = false, unique = true)
  private String vehicleNo;

  @Column(name = "vehicle_type")
  private String vehicleType;

  @Column(name = "registration_date")
  private LocalDate registrationDate;

  public String getVehicleNo() {
    return vehicleNo;
  }

  public void setVehicleNo(String vehicleNo) {
    this.vehicleNo = vehicleNo;
  }

  public String getVehicleType() {
    return vehicleType;
  }

  public void setVehicleType(String vehicleType) {
    this.vehicleType = vehicleType;
  }

  public LocalDate getRegistrationDate() {
    return registrationDate;
  }

  public void setRegistrationDate(LocalDate registrationDate) {
    this.registrationDate = registrationDate;
  }
}
