package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tds_rules")
public class TdsRule extends BaseEntity {
  @Column(nullable = false)
  private String sectionCode;

  @Column(nullable = false)
  private BigDecimal ratePercent;

  @Column(nullable = false)
  private BigDecimal thresholdAmount;

  @Column(nullable = false)
  private LocalDate effectiveFrom;

  private LocalDate effectiveTo;

  public String getSectionCode() {
    return sectionCode;
  }

  public void setSectionCode(String sectionCode) {
    this.sectionCode = sectionCode;
  }

  public BigDecimal getRatePercent() {
    return ratePercent;
  }

  public void setRatePercent(BigDecimal ratePercent) {
    this.ratePercent = ratePercent;
  }

  public BigDecimal getThresholdAmount() {
    return thresholdAmount;
  }

  public void setThresholdAmount(BigDecimal thresholdAmount) {
    this.thresholdAmount = thresholdAmount;
  }

  public LocalDate getEffectiveFrom() {
    return effectiveFrom;
  }

  public void setEffectiveFrom(LocalDate effectiveFrom) {
    this.effectiveFrom = effectiveFrom;
  }

  public LocalDate getEffectiveTo() {
    return effectiveTo;
  }

  public void setEffectiveTo(LocalDate effectiveTo) {
    this.effectiveTo = effectiveTo;
  }
}
