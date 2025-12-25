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
import java.math.BigDecimal;

@Entity
@Table(name = "rfq_awards")
public class RfqAward extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "rfq_line_id")
  private RfqLine rfqLine;

  @ManyToOne
  @JoinColumn(name = "supplier_id")
  private Supplier supplier;

  @Column(nullable = false)
  private BigDecimal awardedQty;

  @Column(nullable = false)
  private BigDecimal rate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DocumentStatus awardStatus;

  public RfqLine getRfqLine() {
    return rfqLine;
  }

  public void setRfqLine(RfqLine rfqLine) {
    this.rfqLine = rfqLine;
  }

  public Supplier getSupplier() {
    return supplier;
  }

  public void setSupplier(Supplier supplier) {
    this.supplier = supplier;
  }

  public BigDecimal getAwardedQty() {
    return awardedQty;
  }

  public void setAwardedQty(BigDecimal awardedQty) {
    this.awardedQty = awardedQty;
  }

  public BigDecimal getRate() {
    return rate;
  }

  public void setRate(BigDecimal rate) {
    this.rate = rate;
  }

  public DocumentStatus getAwardStatus() {
    return awardStatus;
  }

  public void setAwardStatus(DocumentStatus awardStatus) {
    this.awardStatus = awardStatus;
  }
}
