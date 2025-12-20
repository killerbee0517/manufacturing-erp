package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "purchase_invoice_lines")
public class PurchaseInvoiceLine extends BaseEntity {
  @ManyToOne
  private PurchaseInvoice purchaseInvoice;

  @ManyToOne
  private Item item;

  @Column(nullable = false)
  private BigDecimal quantity;

  @Column(nullable = false)
  private BigDecimal lineAmount;

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

  public BigDecimal getQuantity() {
    return quantity;
  }

  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
  }

  public BigDecimal getLineAmount() {
    return lineAmount;
  }

  public void setLineAmount(BigDecimal lineAmount) {
    this.lineAmount = lineAmount;
  }
}
