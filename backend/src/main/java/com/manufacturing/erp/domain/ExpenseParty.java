package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import com.manufacturing.erp.domain.Enums.PayablePartyType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "expense_parties")
public class ExpenseParty extends BaseEntity {
  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "party_type", nullable = false)
  private PayablePartyType partyType;

  @OneToOne
  @JoinColumn(name = "ledger_id")
  private Ledger ledger;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public PayablePartyType getPartyType() {
    return partyType;
  }

  public void setPartyType(PayablePartyType partyType) {
    this.partyType = partyType;
  }

  public Ledger getLedger() {
    return ledger;
  }

  public void setLedger(Ledger ledger) {
    this.ledger = ledger;
  }
}
