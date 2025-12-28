package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "debit_note_lines")
public class DebitNoteLine extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "debit_note_id")
  private DebitNote debitNote;

  @Column(name = "rule_id")
  private Long ruleId;

  private String description;

  @Column(name = "base_value")
  private BigDecimal baseValue;

  private BigDecimal rate;

  private BigDecimal amount;

  private String remarks;

  public DebitNote getDebitNote() {
    return debitNote;
  }

  public void setDebitNote(DebitNote debitNote) {
    this.debitNote = debitNote;
  }

  public Long getRuleId() {
    return ruleId;
  }

  public void setRuleId(Long ruleId) {
    this.ruleId = ruleId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public BigDecimal getBaseValue() {
    return baseValue;
  }

  public void setBaseValue(BigDecimal baseValue) {
    this.baseValue = baseValue;
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

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }
}
