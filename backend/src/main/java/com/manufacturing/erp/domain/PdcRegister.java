package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import com.manufacturing.erp.domain.Enums.PartyRoleType;
import com.manufacturing.erp.domain.Enums.PaymentDirection;
import com.manufacturing.erp.domain.Enums.PdcStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pdc_register")
public class PdcRegister extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @Column(name = "pdc_no", nullable = false)
  private String pdcNo;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "party_id", nullable = false)
  private Party party;

  @Enumerated(EnumType.STRING)
  @Column(name = "party_role_type", nullable = false)
  private PartyRoleType partyRoleType;

  @Enumerated(EnumType.STRING)
  @Column(name = "direction", nullable = false)
  private PaymentDirection direction;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bank_ledger_account_id", nullable = false)
  private LedgerAccount bankLedgerAccount;

  @Column(name = "cheque_number", nullable = false)
  private String chequeNumber;

  @Column(name = "cheque_date", nullable = false)
  private LocalDate chequeDate;

  @Column(nullable = false)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PdcStatus status = PdcStatus.ISSUED;

  @Column
  private String remarks;

  public Company getCompany() {
    return company;
  }

  public void setCompany(Company company) {
    this.company = company;
  }

  public String getPdcNo() {
    return pdcNo;
  }

  public void setPdcNo(String pdcNo) {
    this.pdcNo = pdcNo;
  }

  public Party getParty() {
    return party;
  }

  public void setParty(Party party) {
    this.party = party;
  }

  public PartyRoleType getPartyRoleType() {
    return partyRoleType;
  }

  public void setPartyRoleType(PartyRoleType partyRoleType) {
    this.partyRoleType = partyRoleType;
  }

  public PaymentDirection getDirection() {
    return direction;
  }

  public void setDirection(PaymentDirection direction) {
    this.direction = direction;
  }

  public LedgerAccount getBankLedgerAccount() {
    return bankLedgerAccount;
  }

  public void setBankLedgerAccount(LedgerAccount bankLedgerAccount) {
    this.bankLedgerAccount = bankLedgerAccount;
  }

  public String getChequeNumber() {
    return chequeNumber;
  }

  public void setChequeNumber(String chequeNumber) {
    this.chequeNumber = chequeNumber;
  }

  public LocalDate getChequeDate() {
    return chequeDate;
  }

  public void setChequeDate(LocalDate chequeDate) {
    this.chequeDate = chequeDate;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public PdcStatus getStatus() {
    return status;
  }

  public void setStatus(PdcStatus status) {
    this.status = status;
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }
}
