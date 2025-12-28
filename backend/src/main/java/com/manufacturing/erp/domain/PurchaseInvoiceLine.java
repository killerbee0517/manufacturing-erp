package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import com.manufacturing.erp.domain.Uom;

@Entity
@Table(name = "purchase_invoice_lines")
public class PurchaseInvoiceLine extends BaseEntity {
  @ManyToOne
  private PurchaseInvoice purchaseInvoice;

  @ManyToOne
  private Item item;

  @ManyToOne
  private Uom uom;

  @Column(nullable = false)
  private BigDecimal quantity;

  private BigDecimal rate;

  @Column(nullable = false, name = "line_amount")
  private BigDecimal amount;

  public PurchaseInvoice getPurchaseInvoice() {
    return purchaseInvoice;
  }

  public void setPurchaseInvoice(PurchaseInvoice purchaseInvoice) {
    this.purchaseInvoice = purchaseInvoice;
  }

  public Item getItem() {
    return item;
  }

  public void setItem(Item item) {
    this.item = item;
  }

  public Uom getUom() {
    return uom;
  }

  public void setUom(Uom uom) {
    this.uom = uom;
  }

  public BigDecimal getQuantity() {
    return quantity;
  }

  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
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
}
