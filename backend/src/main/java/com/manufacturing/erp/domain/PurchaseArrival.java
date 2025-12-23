package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "purchase_arrivals")
public class PurchaseArrival extends BaseEntity {
  @ManyToOne
  private PurchaseOrder purchaseOrder;

  @ManyToOne
  private WeighbridgeTicket weighbridgeTicket;

  @ManyToOne
  private Godown godown;

  @Column(nullable = false)
  private BigDecimal unloadingCharges;

  @Column(nullable = false)
  private BigDecimal deductions;

  @Column(nullable = false)
  private BigDecimal tdsPercent;

  @Column(nullable = false)
  private BigDecimal grossAmount;

  @Column(nullable = false)
  private BigDecimal netPayable;

  @Column(nullable = false)
  private Instant createdAt;

  public PurchaseOrder getPurchaseOrder() {
    return purchaseOrder;
  }

  public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
    this.purchaseOrder = purchaseOrder;
  }

  public WeighbridgeTicket getWeighbridgeTicket() {
    return weighbridgeTicket;
  }

  public void setWeighbridgeTicket(WeighbridgeTicket weighbridgeTicket) {
    this.weighbridgeTicket = weighbridgeTicket;
  }

  public Godown getGodown() {
    return godown;
  }

  public void setGodown(Godown godown) {
    this.godown = godown;
  }

  public BigDecimal getUnloadingCharges() {
    return unloadingCharges;
  }

  public void setUnloadingCharges(BigDecimal unloadingCharges) {
    this.unloadingCharges = unloadingCharges;
  }

  public BigDecimal getDeductions() {
    return deductions;
  }

  public void setDeductions(BigDecimal deductions) {
    this.deductions = deductions;
  }

  public BigDecimal getTdsPercent() {
    return tdsPercent;
  }

  public void setTdsPercent(BigDecimal tdsPercent) {
    this.tdsPercent = tdsPercent;
  }

  public BigDecimal getGrossAmount() {
    return grossAmount;
  }

  public void setGrossAmount(BigDecimal grossAmount) {
    this.grossAmount = grossAmount;
  }

  public BigDecimal getNetPayable() {
    return netPayable;
  }

  public void setNetPayable(BigDecimal netPayable) {
    this.netPayable = netPayable;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
