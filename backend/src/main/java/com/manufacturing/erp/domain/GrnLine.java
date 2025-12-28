package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import com.manufacturing.erp.domain.PurchaseOrderLine;

@Entity
@Table(name = "grn_lines")
public class GrnLine extends BaseEntity {
  @ManyToOne
  private Grn grn;

  @ManyToOne
  @JoinColumn(name = "po_line_id")
  private PurchaseOrderLine purchaseOrderLine;

  @ManyToOne
  private Item item;

  @ManyToOne
  private Uom uom;

  private String bagType;

  private Integer bagCount;

  @Column(name = "quantity")
  private BigDecimal quantity;

  @Column(name = "expected_qty")
  private BigDecimal expectedQty;

  @Column(name = "received_qty")
  private BigDecimal receivedQty;

  @Column(name = "accepted_qty")
  private BigDecimal acceptedQty;

  @Column(name = "rejected_qty")
  private BigDecimal rejectedQty;

  private BigDecimal weight;

  private BigDecimal rate;

  private BigDecimal amount;

  public Grn getGrn() {
    return grn;
  }

  public void setGrn(Grn grn) {
    this.grn = grn;
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

  public String getBagType() {
    return bagType;
  }

  public void setBagType(String bagType) {
    this.bagType = bagType;
  }

  public Integer getBagCount() {
    return bagCount;
  }

  public void setBagCount(Integer bagCount) {
    this.bagCount = bagCount;
  }

  public BigDecimal getQuantity() {
    return quantity;
  }

  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
  }

  public BigDecimal getExpectedQty() {
    return expectedQty;
  }

  public void setExpectedQty(BigDecimal expectedQty) {
    this.expectedQty = expectedQty;
  }

  public BigDecimal getReceivedQty() {
    return receivedQty;
  }

  public void setReceivedQty(BigDecimal receivedQty) {
    this.receivedQty = receivedQty;
  }

  public BigDecimal getAcceptedQty() {
    return acceptedQty;
  }

  public void setAcceptedQty(BigDecimal acceptedQty) {
    this.acceptedQty = acceptedQty;
  }

  public BigDecimal getRejectedQty() {
    return rejectedQty;
  }

  public void setRejectedQty(BigDecimal rejectedQty) {
    this.rejectedQty = rejectedQty;
  }

  public BigDecimal getWeight() {
    return weight;
  }

  public void setWeight(BigDecimal weight) {
    this.weight = weight;
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

  public PurchaseOrderLine getPurchaseOrderLine() {
    return purchaseOrderLine;
  }

  public void setPurchaseOrderLine(PurchaseOrderLine purchaseOrderLine) {
    this.purchaseOrderLine = purchaseOrderLine;
  }
}
