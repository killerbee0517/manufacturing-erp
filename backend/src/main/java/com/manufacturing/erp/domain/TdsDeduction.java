package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "tds_deductions")
public class TdsDeduction extends BaseEntity {
  @ManyToOne
  private PurchaseInvoice purchaseInvoice;

  @Column(nullable = false)
  private String sectionCode;

  @Column(nullable = false)
  private BigDecimal tdsAmount;

  public PurchaseInvoice getPurchaseInvoice() {
    return purchaseInvoice;
  }

  public void setPurchaseInvoice(PurchaseInvoice purchaseInvoice) {
    this.purchaseInvoice = purchaseInvoice;
  }

  public String getSectionCode() {
    return sectionCode;
  }

  public void setSectionCode(String sectionCode) {
    this.sectionCode = sectionCode;
  }

  public BigDecimal getTdsAmount() {
    return tdsAmount;
  }

  public void setTdsAmount(BigDecimal tdsAmount) {
    this.tdsAmount = tdsAmount;
  }
}
