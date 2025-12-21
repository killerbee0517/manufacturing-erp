package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "rfq")
public class Rfq extends BaseEntity {
  @Column(name = "rfq_no", nullable = false)
  private String rfqNo;

  @Column(nullable = false)
  private String status;

  public String getRfqNo() {
    return rfqNo;
  }

  public void setRfqNo(String rfqNo) {
    this.rfqNo = rfqNo;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }
}
