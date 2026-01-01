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
import java.time.LocalDate;

@Entity
@Table(name = "process_runs")
public class ProcessRun extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @ManyToOne
  @JoinColumn(name = "production_batch_id", nullable = false)
  private ProductionBatch productionBatch;

  @ManyToOne
  @JoinColumn(name = "process_step_id")
  private ProcessStep processStep;

  @Column(name = "run_no")
  private Integer runNo;

  @Column(name = "step_no")
  private Integer stepNo;

  @Column(name = "run_date", nullable = false)
  private LocalDate runDate;

  @Column(name = "notes")
  private String notes;

  @Column(name = "moisture_percent")
  private java.math.BigDecimal moisturePercent;

  @Column(name = "step_name")
  private String stepName;

  @Column(name = "started_at")
  private Instant startedAt;

  @Column(name = "ended_at")
  private Instant endedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ProductionStatus status;

  public ProductionBatch getProductionBatch() {
    return productionBatch;
  }

  public void setProductionBatch(ProductionBatch productionBatch) {
    this.productionBatch = productionBatch;
  }

  public Company getCompany() {
    return company;
  }

  public void setCompany(Company company) {
    this.company = company;
  }

  public ProcessStep getProcessStep() {
    return processStep;
  }

  public void setProcessStep(ProcessStep processStep) {
    this.processStep = processStep;
  }

  public Integer getRunNo() {
    return runNo;
  }

  public void setRunNo(Integer runNo) {
    this.runNo = runNo;
  }

  public Integer getStepNo() {
    return stepNo;
  }

  public void setStepNo(Integer stepNo) {
    this.stepNo = stepNo;
  }

  public String getStepName() {
    return stepName;
  }

  public void setStepName(String stepName) {
    this.stepName = stepName;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public java.math.BigDecimal getMoisturePercent() {
    return moisturePercent;
  }

  public void setMoisturePercent(java.math.BigDecimal moisturePercent) {
    this.moisturePercent = moisturePercent;
  }

  public Instant getStartedAt() {
    return startedAt;
  }

  public void setStartedAt(Instant startedAt) {
    this.startedAt = startedAt;
  }

  public Instant getEndedAt() {
    return endedAt;
  }

  public void setEndedAt(Instant endedAt) {
    this.endedAt = endedAt;
  }

  public LocalDate getRunDate() {
    return runDate;
  }

  public void setRunDate(LocalDate runDate) {
    this.runDate = runDate;
  }

  public ProductionStatus getStatus() {
    return status;
  }

  public void setStatus(ProductionStatus status) {
    this.status = status;
  }
}
