package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "process_template_steps")
public class ProcessTemplateStep extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "template_id", nullable = false)
  private ProcessTemplate template;

  @Column(name = "step_no", nullable = false)
  private Integer stepNo;

  @Column(name = "step_name", nullable = false)
  private String stepName;

  @Enumerated(EnumType.STRING)
  @Column(name = "step_type", nullable = false)
  private StepType stepType = StepType.PROCESS;

  private String notes;

  public enum StepType {
    CONSUME,
    PROCESS,
    PRODUCE,
    QUALITY
  }

  public ProcessTemplate getTemplate() {
    return template;
  }

  public void setTemplate(ProcessTemplate template) {
    this.template = template;
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

  public StepType getStepType() {
    return stepType;
  }

  public void setStepType(StepType stepType) {
    this.stepType = stepType;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }
}
