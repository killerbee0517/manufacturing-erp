package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "rfq_lines")
public class RfqLine extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "rfq_id")
  private Rfq rfq;

  @ManyToOne
  @JoinColumn(name = "item_id")
  private Item item;

  @ManyToOne
  @JoinColumn(name = "uom_id")
  private Uom uom;

  @ManyToOne
  @JoinColumn(name = "broker_id")
  private Broker broker;

  @Column(nullable = false)
  private BigDecimal quantity;

  @Column(name = "rate_expected")
  private BigDecimal rateExpected;

  private String remarks;

  public Rfq getRfq() {
    return rfq;
  }

  public void setRfq(Rfq rfq) {
    this.rfq = rfq;
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

  public Broker getBroker() {
    return broker;
  }

  public void setBroker(Broker broker) {
    this.broker = broker;
  }

  public BigDecimal getQuantity() {
    return quantity;
  }

  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
  }

  public BigDecimal getRateExpected() {
    return rateExpected;
  }

  public void setRateExpected(BigDecimal rateExpected) {
    this.rateExpected = rateExpected;
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }
}
