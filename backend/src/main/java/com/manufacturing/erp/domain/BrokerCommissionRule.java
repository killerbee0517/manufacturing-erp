package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "broker_commission_rules")
public class BrokerCommissionRule extends BaseEntity {
  @ManyToOne
  private Broker broker;

  @Column(nullable = false)
  private BigDecimal ratePercent;

  public Broker getBroker() {
    return broker;
  }

  public void setBroker(Broker broker) {
    this.broker = broker;
  }

  public BigDecimal getRatePercent() {
    return ratePercent;
  }

  public void setRatePercent(BigDecimal ratePercent) {
    this.ratePercent = ratePercent;
  }
}
