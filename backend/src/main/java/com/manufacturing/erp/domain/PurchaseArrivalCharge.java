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
@Table(name = "purchase_arrival_charges")
public class PurchaseArrivalCharge extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "purchase_arrival_id")
  private PurchaseArrival purchaseArrival;

  @ManyToOne
  @JoinColumn(name = "charge_type_id")
  private DeductionChargeType chargeType;

  @Enumerated(EnumType.STRING)
  @Column(name = "calc_type", nullable = false)
  private CalcType calcType;

  private BigDecimal rate;

  @Column(nullable = false)
  private BigDecimal amount;

  @Column(name = "is_deduction", nullable = false)
  private boolean deduction;

  @Enumerated(EnumType.STRING)
  @Column(name = "payable_party_type", nullable = false)
  private PayablePartyType payablePartyType;

  @Column(name = "payable_party_id")
  private Long payablePartyId;

  private String remarks;

  public PurchaseArrival getPurchaseArrival() {
    return purchaseArrival;
  }

  public void setPurchaseArrival(PurchaseArrival purchaseArrival) {
    this.purchaseArrival = purchaseArrival;
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

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
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
