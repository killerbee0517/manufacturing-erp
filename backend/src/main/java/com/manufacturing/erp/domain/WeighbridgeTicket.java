package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import com.manufacturing.erp.domain.Enums.DocumentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import com.manufacturing.erp.domain.PurchaseOrder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "weighbridge_tickets")
public class WeighbridgeTicket extends BaseEntity {
  @Column(name = "ticket_no", nullable = false)
  private String serialNo;

  @ManyToOne
  @JoinColumn(name = "vehicle_id")
  private Vehicle vehicle;

  @ManyToOne
  @JoinColumn(name = "po_id")
  private PurchaseOrder purchaseOrder;

  @ManyToOne
  private Supplier supplier;

  @ManyToOne
  private Item item;

  @Column(name = "date_in", nullable = false)
  private LocalDate dateIn;

  @Column(name = "time_in", nullable = false)
  private LocalTime timeIn;

  @Column(name = "date_out")
  private LocalDate secondDate;

  @Column(name = "time_out")
  private LocalTime secondTime;

  @Column(name = "gross_weight", nullable = false)
  private BigDecimal grossWeight;

  @Column(name = "tare_weight")
  private BigDecimal unloadedWeight;

  @Column(name = "net_weight", nullable = false)
  private BigDecimal netWeight;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DocumentStatus status;

  public String getSerialNo() {
    return serialNo;
  }

  public void setSerialNo(String serialNo) {
    this.serialNo = serialNo;
  }

  public Vehicle getVehicle() {
    return vehicle;
  }

  public void setVehicle(Vehicle vehicle) {
    this.vehicle = vehicle;
  }

  public PurchaseOrder getPurchaseOrder() {
    return purchaseOrder;
  }

  public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
    this.purchaseOrder = purchaseOrder;
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

  public LocalDate getSecondDate() {
    return secondDate;
  }

  public void setSecondDate(LocalDate secondDate) {
    this.secondDate = secondDate;
  }

  public LocalTime getSecondTime() {
    return secondTime;
  }

  public void setSecondTime(LocalTime secondTime) {
    this.secondTime = secondTime;
  }

  public BigDecimal getGrossWeight() {
    return grossWeight;
  }

  public void setGrossWeight(BigDecimal grossWeight) {
    this.grossWeight = grossWeight;
  }

  public BigDecimal getUnloadedWeight() {
    return unloadedWeight;
  }

  public void setUnloadedWeight(BigDecimal unloadedWeight) {
    this.unloadedWeight = unloadedWeight;
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
