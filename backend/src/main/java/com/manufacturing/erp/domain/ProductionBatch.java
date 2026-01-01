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
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "production_batches")
public class ProductionBatch extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @Column(name = "batch_no", nullable = false)
  private String batchNo;

  @ManyToOne
  @JoinColumn(name = "production_order_id")
  private ProductionOrder productionOrder;

  @ManyToOne
  @JoinColumn(name = "template_id")
  private ProcessTemplate template;

  @ManyToOne
  @JoinColumn(name = "uom_id")
  private Uom uom;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ProductionStatus status;

  @Column(name = "planned_output_qty")
  private BigDecimal plannedOutputQty;

  @Column(name = "start_date")
  private LocalDate startDate;

  @Column(name = "end_date")
  private LocalDate endDate;

  private Instant startedAt;

  private Instant completedAt;

  private String remarks;

  public String getBatchNo() {
    return batchNo;
  }

  public void setBatchNo(String batchNo) {
    this.batchNo = batchNo;
  }

  public Company getCompany() {
    return company;
  }

  public void setCompany(Company company) {
    this.company = company;
  }

  public ProductionOrder getProductionOrder() {
    return productionOrder;
  }

  public void setProductionOrder(ProductionOrder productionOrder) {
    this.productionOrder = productionOrder;
  }

  public ProcessTemplate getTemplate() {
    return template;
  }

  public void setTemplate(ProcessTemplate template) {
    this.template = template;
  }

  public Uom getUom() {
    return uom;
  }

  public void setUom(Uom uom) {
    this.uom = uom;
  }

  public ProductionStatus getStatus() {
    return status;
  }

  public void setStatus(ProductionStatus status) {
    this.status = status;
  }

  public BigDecimal getPlannedOutputQty() {
    return plannedOutputQty;
  }

  public void setPlannedOutputQty(BigDecimal plannedOutputQty) {
    this.plannedOutputQty = plannedOutputQty;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
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

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }
}
