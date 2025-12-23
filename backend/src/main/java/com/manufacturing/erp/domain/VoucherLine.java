package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "voucher_lines")
public class VoucherLine extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "voucher_id", nullable = false)
  private Voucher voucher;

  @ManyToOne
  @JoinColumn(name = "ledger_id", nullable = false)
  private Ledger ledger;

  @Column(name = "dr_amount", nullable = false)
  private BigDecimal drAmount;

  @Column(name = "cr_amount", nullable = false)
  private BigDecimal crAmount;

  public Voucher getVoucher() {
    return voucher;
  }

  public void setVoucher(Voucher voucher) {
    this.voucher = voucher;
  }

  public Ledger getLedger() {
    return ledger;
  }

  public void setLedger(Ledger ledger) {
    this.ledger = ledger;
  }

  public BigDecimal getDrAmount() {
    return drAmount;
  }

  public void setDrAmount(BigDecimal drAmount) {
    this.drAmount = drAmount;
  }

  public BigDecimal getCrAmount() {
    return crAmount;
  }

  public void setCrAmount(BigDecimal crAmount) {
    this.crAmount = crAmount;
  }
}
