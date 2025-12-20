package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import com.manufacturing.erp.domain.Enums.DocumentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "grn")
public class Grn extends BaseEntity {
  @Column(nullable = false)
  private String grnNo;

  @ManyToOne
  private Supplier supplier;

  @ManyToOne
  private WeighbridgeTicket weighbridgeTicket;

  @Column(nullable = false)
  private LocalDate grnDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DocumentStatus status;

  public String getGrnNo() {
    return grnNo;
  }

  public void setGrnNo(String grnNo) {
    this.grnNo = grnNo;
  }

  public Supplier getSupplier() {
    return supplier;
  }

  public void setSupplier(Supplier supplier) {
    this.supplier = supplier;
  }

  public WeighbridgeTicket getWeighbridgeTicket() {
    return weighbridgeTicket;
  }

  public void setWeighbridgeTicket(WeighbridgeTicket weighbridgeTicket) {
    this.weighbridgeTicket = weighbridgeTicket;
  }

  public LocalDate getGrnDate() {
    return grnDate;
  }

  public void setGrnDate(LocalDate grnDate) {
    this.grnDate = grnDate;
  }

  public DocumentStatus getStatus() {
    return status;
  }

  public void setStatus(DocumentStatus status) {
    this.status = status;
  }
}
