package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import com.manufacturing.erp.domain.Enums.CalcType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "deduction_charge_types")
public class DeductionChargeType extends BaseEntity {
  @Column(nullable = false, unique = true)
  private String code;

  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "default_calc_type", nullable = false)
  private CalcType defaultCalcType;

  private java.math.BigDecimal defaultRate;

  @Column(name = "is_deduction", nullable = false)
  private boolean deduction;

  @Column(nullable = false)
  private boolean enabled = true;

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public CalcType getDefaultCalcType() {
    return defaultCalcType;
  }

  public void setDefaultCalcType(CalcType defaultCalcType) {
    this.defaultCalcType = defaultCalcType;
  }

  public java.math.BigDecimal getDefaultRate() {
    return defaultRate;
  }

  public void setDefaultRate(java.math.BigDecimal defaultRate) {
    this.defaultRate = defaultRate;
  }

  public boolean isDeduction() {
    return deduction;
  }

  public void setDeduction(boolean deduction) {
    this.deduction = deduction;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
}
