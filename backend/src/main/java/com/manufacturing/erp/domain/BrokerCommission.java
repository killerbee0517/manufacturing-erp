package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "broker_commissions")
public class BrokerCommission extends BaseEntity {
  @ManyToOne
  private SalesInvoice salesInvoice;

  @ManyToOne
  private Broker broker;

  @Column(nullable = false)
  private BigDecimal commissionAmount;

  public SalesInvoice getSalesInvoice() {
    return salesInvoice;
  }

  public void setSalesInvoice(SalesInvoice salesInvoice) {
    this.salesInvoice = salesInvoice;
  }

  public Broker getBroker() {
    return broker;
  }

  public void setBroker(Broker broker) {
    this.broker = broker;
  }

  public BigDecimal getCommissionAmount() {
    return commissionAmount;
  }

  public void setCommissionAmount(BigDecimal commissionAmount) {
    this.commissionAmount = commissionAmount;
  }
}
