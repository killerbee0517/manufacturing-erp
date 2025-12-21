package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import com.manufacturing.erp.domain.Enums.DocumentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "weighbridge_tickets")
public class WeighbridgeTicket extends BaseEntity {
  @Column(nullable = false)
  private String ticketNo;

  @Column(nullable = false)
  private String vehicleNo;

  @ManyToOne
  private Supplier supplier;

  @ManyToOne
  private Item item;

  @Column(nullable = false)
  private LocalDate dateIn;

  @Column(nullable = false)
  private LocalTime timeIn;

  private LocalDate dateOut;

  private LocalTime timeOut;

  @Column(nullable = false)
  private BigDecimal grossWeight;

  @Column(nullable = false)
  private BigDecimal tareWeight;

  @Column(nullable = false)
  private BigDecimal netWeight;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DocumentStatus status;

  public String getTicketNo() {
    return ticketNo;
  }

  public void setTicketNo(String ticketNo) {
    this.ticketNo = ticketNo;
  }

  public String getVehicleNo() {
    return vehicleNo;
  }

  public void setVehicleNo(String vehicleNo) {
    this.vehicleNo = vehicleNo;
  }

  public Supplier getSupplier() {
    return supplier;
  }

  public void setSupplier(Supplier supplier) {
    this.supplier = supplier;
  }

  public Item getItem() {
    return item;
  }

  public void setItem(Item item) {
    this.item = item;
  }

  public LocalDate getDateIn() {
    return dateIn;
  }

  public void setDateIn(LocalDate dateIn) {
    this.dateIn = dateIn;
  }

  public LocalTime getTimeIn() {
    return timeIn;
  }

  public void setTimeIn(LocalTime timeIn) {
    this.timeIn = timeIn;
  }

  public LocalDate getDateOut() {
    return dateOut;
  }

  public void setDateOut(LocalDate dateOut) {
    this.dateOut = dateOut;
  }

  public LocalTime getTimeOut() {
    return timeOut;
  }

  public void setTimeOut(LocalTime timeOut) {
    this.timeOut = timeOut;
  }

  public BigDecimal getGrossWeight() {
    return grossWeight;
  }

  public void setGrossWeight(BigDecimal grossWeight) {
    this.grossWeight = grossWeight;
  }

  public BigDecimal getTareWeight() {
    return tareWeight;
  }

  public void setTareWeight(BigDecimal tareWeight) {
    this.tareWeight = tareWeight;
  }

  public BigDecimal getNetWeight() {
    return netWeight;
  }

  public void setNetWeight(BigDecimal netWeight) {
    this.netWeight = netWeight;
  }

  public DocumentStatus getStatus() {
    return status;
  }

  public void setStatus(DocumentStatus status) {
    this.status = status;
  }
}
