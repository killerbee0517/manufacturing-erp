package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "process_steps")
public class ProcessStep extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "template_id", nullable = false)
  private ProcessTemplate template;

  @Column(nullable = false)
  private String name;

  private String description;

  @Column(name = "sequence_no", nullable = false)
  private Integer sequenceNo;

  @ManyToOne
  @JoinColumn(name = "source_godown_id")
  private Godown sourceGodown;

  @ManyToOne
  @JoinColumn(name = "dest_godown_id")
  private Godown destGodown;

  public ProcessTemplate getTemplate() {
    return template;
  }

  public void setTemplate(ProcessTemplate template) {
    this.template = template;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Integer getSequenceNo() {
    return sequenceNo;
  }

  public void setSequenceNo(Integer sequenceNo) {
    this.sequenceNo = sequenceNo;
  }

  public Godown getSourceGodown() {
    return sourceGodown;
  }

  public void setSourceGodown(Godown sourceGodown) {
    this.sourceGodown = sourceGodown;
  }

  public Godown getDestGodown() {
    return destGodown;
  }

  public void setDestGodown(Godown destGodown) {
    this.destGodown = destGodown;
  }
}
