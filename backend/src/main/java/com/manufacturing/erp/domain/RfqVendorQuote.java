package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "rfq_vendor_quotes")
public class RfqVendorQuote extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "rfq_id")
  private Rfq rfq;

  @ManyToOne
  @JoinColumn(name = "supplier_id")
  private Supplier supplier;

  @ManyToOne
  @JoinColumn(name = "item_id")
  private Item item;

  @Column(name = "quoted_rate")
  private BigDecimal quotedRate;

  @Column(name = "quoted_qty")
  private BigDecimal quotedQty;

  private String remarks;

  private String status;

  public Rfq getRfq() {
    return rfq;
  }

  public void setRfq(Rfq rfq) {
    this.rfq = rfq;
  }

  public Supplier getSupplier() {
    return supplier;
  }

  public void setSupplier(Supplier supplier) {
    this.supplier = supplier;
  }

  public Item getItem() {
    return item;
  }

  public void setItem(Item item) {
    this.item = item;
  }

  public BigDecimal getQuotedRate() {
    return quotedRate;
  }

  public void setQuotedRate(BigDecimal quotedRate) {
    this.quotedRate = quotedRate;
  }

  public BigDecimal getQuotedQty() {
    return quotedQty;
  }

  public void setQuotedQty(BigDecimal quotedQty) {
    this.quotedQty = quotedQty;
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
