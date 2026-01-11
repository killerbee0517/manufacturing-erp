package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import com.manufacturing.erp.domain.Enums.CalcType;
import com.manufacturing.erp.domain.Enums.PayablePartyType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "process_template_step_charges")
public class ProcessTemplateStepCharge extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "step_id", nullable = false)
  private ProcessTemplateStep step;

  @ManyToOne
  @JoinColumn(name = "charge_type_id", nullable = false)
  private DeductionChargeType chargeType;

  @Enumerated(EnumType.STRING)
  @Column(name = "calc_type", nullable = false)
  private CalcType calcType = CalcType.FLAT;

  private BigDecimal rate;

  @Column(name = "per_qty", nullable = false)
  private boolean perQty;

  @Column(name = "is_deduction", nullable = false)
  private boolean deduction;

  @Enumerated(EnumType.STRING)
  @Column(name = "payable_party_type", nullable = false)
  private PayablePartyType payablePartyType;

  @Column(name = "payable_party_id")
  private Long payablePartyId;

  private String remarks;

  public ProcessTemplateStep getStep() {
    return step;
  }

  public void setStep(ProcessTemplateStep step) {
    this.step = step;
  }

  public DeductionChargeType getChargeType() {
    return chargeType;
  }

  public void setChargeType(DeductionChargeType chargeType) {
    this.chargeType = chargeType;
  }

  public CalcType getCalcType() {
    return calcType;
  }

  public void setCalcType(CalcType calcType) {
    this.calcType = calcType;
  }

  public BigDecimal getRate() {
    return rate;
  }

  public void setRate(BigDecimal rate) {
    this.rate = rate;
  }

  public boolean isPerQty() {
    return perQty;
  }

  public void setPerQty(boolean perQty) {
    this.perQty = perQty;
  }

  public boolean isDeduction() {
    return deduction;
  }

  public void setDeduction(boolean deduction) {
    this.deduction = deduction;
  }

  public PayablePartyType getPayablePartyType() {
    return payablePartyType;
  }

  public void setPayablePartyType(PayablePartyType payablePartyType) {
    this.payablePartyType = payablePartyType;
  }

  public Long getPayablePartyId() {
    return payablePartyId;
  }

  public void setPayablePartyId(Long payablePartyId) {
    this.payablePartyId = payablePartyId;
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }
}
