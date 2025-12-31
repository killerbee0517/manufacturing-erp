package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import com.manufacturing.erp.domain.Enums.LedgerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ledger_accounts")
public class LedgerAccount extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @Enumerated(EnumType.STRING)
  @Column(name = "ledger_type", nullable = false)
  private LedgerType ledgerType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "party_id")
  private Party party;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bank_id")
  private Bank bank;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private boolean active = true;

  public Company getCompany() {
    return company;
  }

  public void setCompany(Company company) {
    this.company = company;
  }

  public LedgerType getLedgerType() {
    return ledgerType;
  }

  public void setLedgerType(LedgerType ledgerType) {
    this.ledgerType = ledgerType;
  }

  public Party getParty() {
    return party;
  }

  public void setParty(Party party) {
    this.party = party;
  }

  public Bank getBank() {
    return bank;
  }

  public void setBank(Bank bank) {
    this.bank = bank;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}
