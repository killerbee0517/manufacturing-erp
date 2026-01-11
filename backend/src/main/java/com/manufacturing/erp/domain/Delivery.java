package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "deliveries")
public class Delivery extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @Column(name = "delivery_no", nullable = false)
  private String deliveryNo;

  @ManyToOne
  private SalesOrder salesOrder;

  public String getDeliveryNo() {
    return deliveryNo;
  }

  public Company getCompany() {
    return company;
  }

  public void setCompany(Company company) {
    this.company = company;
  }

  public void setDeliveryNo(String deliveryNo) {
    this.deliveryNo = deliveryNo;
  }

  public SalesOrder getSalesOrder() {
    return salesOrder;
  }

  public void setSalesOrder(SalesOrder salesOrder) {
    this.salesOrder = salesOrder;
  }
}
