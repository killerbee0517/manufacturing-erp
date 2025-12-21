package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder extends BaseEntity {
  @Column(name = "po_no", nullable = false)
  private String poNo;

  @ManyToOne
  private Supplier supplier;

  @Column(nullable = false)
  private String status;

  public String getPoNo() {
    return poNo;
  }

  public void setPoNo(String poNo) {
    this.poNo = poNo;
  }

  public Supplier getSupplier() {
    return supplier;
  }

  public void setSupplier(Supplier supplier) {
    this.supplier = supplier;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
