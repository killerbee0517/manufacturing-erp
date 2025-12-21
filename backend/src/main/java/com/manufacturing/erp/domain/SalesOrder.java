package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "sales_orders")
public class SalesOrder extends BaseEntity {
  @Column(name = "so_no", nullable = false)
  private String soNo;

  @ManyToOne
  private Customer customer;

  @Column(nullable = false)
  private String status;

  public String getSoNo() {
    return soNo;
  }

  public void setSoNo(String soNo) {
    this.soNo = soNo;
  }

  public Customer getCustomer() {
    return customer;
  }

  public void setCustomer(Customer customer) {
    this.customer = customer;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
