package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "production_batch_steps")
public class ProductionBatchStep extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "batch_id", nullable = false)
  private ProductionBatch batch;

  @Column(name = "step_no", nullable = false)
  private Integer stepNo;

  @Column(name = "step_name", nullable = false)
  private String stepName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StepStatus status = StepStatus.PENDING;

  @Column(name = "started_at")
  private Instant startedAt;

  @Column(name = "completed_at")
  private Instant completedAt;

  private String notes;

  public enum StepStatus {
    PENDING,
    DONE,
    SKIPPED
  }

  public ProductionBatch getBatch() {
    return batch;
  }

  public void setBatch(ProductionBatch batch) {
    this.batch = batch;
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

  public StepStatus getStatus() {
    return status;
  }

  public void setStatus(StepStatus status) {
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

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }
}
