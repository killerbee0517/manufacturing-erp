package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import com.manufacturing.erp.domain.Enums.ProductionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "production_batches")
public class ProductionBatch extends BaseEntity {
  @Column(name = "batch_no", nullable = false)
  private String batchNo;

  @ManyToOne
  @JoinColumn(name = "production_order_id", nullable = false)
  private ProductionOrder productionOrder;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ProductionStatus status;

  private Instant startedAt;

  private Instant completedAt;

  public String getBatchNo() {
    return batchNo;
  }

  public void setBatchNo(String batchNo) {
    this.batchNo = batchNo;
  }

  public ProductionOrder getProductionOrder() {
    return productionOrder;
  }

  public void setProductionOrder(ProductionOrder productionOrder) {
    this.productionOrder = productionOrder;
  }

  public ProductionStatus getStatus() {
    return status;
  }

  public void setStatus(ProductionStatus status) {
    this.status = status;
  }

  public Instant getStartedAt() {
    return startedAt;
  }

  public void setStartedAt(Instant startedAt) {
    this.startedAt = startedAt;
  }

  public Instant getCompletedAt() {
    return completedAt;
  }

  public void setCompletedAt(Instant completedAt) {
    this.completedAt = completedAt;
  }
}
