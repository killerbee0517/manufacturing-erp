package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import com.manufacturing.erp.domain.Enums.BrokerCommissionType;
import com.manufacturing.erp.domain.Enums.BrokeragePaidBy;
import com.manufacturing.erp.domain.Enums.PartyRoleType;
import com.manufacturing.erp.domain.Enums.SupplierType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "party_roles")
public class PartyRole extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "party_id", nullable = false)
  private Party party;

  @Enumerated(EnumType.STRING)
  @Column(name = "role_type", nullable = false)
  private PartyRoleType roleType;

  @Column(name = "credit_period_days")
  private Integer creditPeriodDays;

  @Enumerated(EnumType.STRING)
  @Column(name = "supplier_type")
  private SupplierType supplierType;

  @Enumerated(EnumType.STRING)
  @Column(name = "broker_commission_type")
  private BrokerCommissionType brokerCommissionType;

  @Column(name = "broker_commission_rate")
  private BigDecimal brokerCommissionRate;

  @Enumerated(EnumType.STRING)
  @Column(name = "brokerage_paid_by")
  private BrokeragePaidBy brokeragePaidBy;

  @Column(nullable = false)
  private boolean active = true;

  public Company getCompany() {
    return company;
  }

  public void setCompany(Company company) {
    this.company = company;
  }

  public Party getParty() {
    return party;
  }

  public void setParty(Party party) {
    this.party = party;
  }

  public PartyRoleType getRoleType() {
    return roleType;
  }

  public void setRoleType(PartyRoleType roleType) {
    this.roleType = roleType;
  }

  public Integer getCreditPeriodDays() {
    return creditPeriodDays;
  }

  public void setCreditPeriodDays(Integer creditPeriodDays) {
    this.creditPeriodDays = creditPeriodDays;
  }

  public SupplierType getSupplierType() {
    return supplierType;
  }

  public void setSupplierType(SupplierType supplierType) {
    this.supplierType = supplierType;
  }

  public BrokerCommissionType getBrokerCommissionType() {
    return brokerCommissionType;
  }

  public void setBrokerCommissionType(BrokerCommissionType brokerCommissionType) {
    this.brokerCommissionType = brokerCommissionType;
  }

  public BigDecimal getBrokerCommissionRate() {
    return brokerCommissionRate;
  }

  public void setBrokerCommissionRate(BigDecimal brokerCommissionRate) {
    this.brokerCommissionRate = brokerCommissionRate;
  }

  public BrokeragePaidBy getBrokeragePaidBy() {
    return brokeragePaidBy;
  }

  public void setBrokeragePaidBy(BrokeragePaidBy brokeragePaidBy) {
    this.brokeragePaidBy = brokeragePaidBy;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}
