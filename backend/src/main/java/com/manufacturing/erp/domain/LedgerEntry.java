package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "ledger_account_id", nullable = false)
  private LedgerAccount ledgerAccount;

  @Column(name = "txn_date", nullable = false)
  private LocalDate txnDate;

  @Column(name = "txn_type", nullable = false)
  private String txnType;

  @Column(name = "ref_table")
  private String refTable;

  @Column(name = "ref_id")
  private Long refId;

  @Column(nullable = false)
  private BigDecimal debit = BigDecimal.ZERO;

  @Column(nullable = false)
  private BigDecimal credit = BigDecimal.ZERO;

  @Column
  private String narration;

  public Company getCompany() {
    return company;
  }

  public void setCompany(Company company) {
    this.company = company;
  }

  public LedgerAccount getLedgerAccount() {
    return ledgerAccount;
  }

  public void setLedgerAccount(LedgerAccount ledgerAccount) {
    this.ledgerAccount = ledgerAccount;
  }

  public LocalDate getTxnDate() {
    return txnDate;
  }

  public void setTxnDate(LocalDate txnDate) {
    this.txnDate = txnDate;
  }

  public String getTxnType() {
    return txnType;
  }

  public void setTxnType(String txnType) {
    this.txnType = txnType;
  }

  public String getRefTable() {
    return refTable;
  }

  public void setRefTable(String refTable) {
    this.refTable = refTable;
  }

  public Long getRefId() {
    return refId;
  }

  public void setRefId(Long refId) {
    this.refId = refId;
  }

  public BigDecimal getDebit() {
    return debit;
  }

  public void setDebit(BigDecimal debit) {
    this.debit = debit;
  }

  public BigDecimal getCredit() {
    return credit;
  }

  public void setCredit(BigDecimal credit) {
    this.credit = credit;
  }

  public String getNarration() {
    return narration;
  }

  public void setNarration(String narration) {
    this.narration = narration;
  }
}
