package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "vouchers")
public class Voucher extends BaseEntity {
  @Column(name = "voucher_no", nullable = false)
  private String voucherNo;

  @Column(name = "voucher_date", nullable = false)
  private LocalDate voucherDate;

  private String narration;

  private String referenceType;

  private Long referenceId;

  public String getVoucherNo() {
    return voucherNo;
  }

  public void setVoucherNo(String voucherNo) {
    this.voucherNo = voucherNo;
  }

  public LocalDate getVoucherDate() {
    return voucherDate;
  }

  public void setVoucherDate(LocalDate voucherDate) {
    this.voucherDate = voucherDate;
  }

  public String getNarration() {
    return narration;
  }

  public void setNarration(String narration) {
    this.narration = narration;
  }

  public String getReferenceType() {
    return referenceType;
  }

  public void setReferenceType(String referenceType) {
    this.referenceType = referenceType;
  }

  public Long getReferenceId() {
    return referenceId;
  }

  public void setReferenceId(Long referenceId) {
    this.referenceId = referenceId;
  }
}
