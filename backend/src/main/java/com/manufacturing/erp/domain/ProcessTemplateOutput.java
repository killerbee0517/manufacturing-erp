package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import com.manufacturing.erp.domain.Enums.ProcessOutputType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "process_template_outputs")
public class ProcessTemplateOutput extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "template_id", nullable = false)
  private ProcessTemplate template;

  @ManyToOne
  @JoinColumn(name = "item_id", nullable = false)
  private Item item;

  @ManyToOne
  @JoinColumn(name = "uom_id", nullable = false)
  private Uom uom;

  @Enumerated(EnumType.STRING)
  @Column(name = "output_type", nullable = false)
  private ProcessOutputType outputType;

  @Column(name = "default_ratio", nullable = false)
  private BigDecimal defaultRatio;

  private String notes;

  public ProcessTemplate getTemplate() {
    return template;
  }

  public void setTemplate(ProcessTemplate template) {
    this.template = template;
  }

  public Item getItem() {
    return item;
  }

  public void setItem(Item item) {
    this.item = item;
  }

  public Uom getUom() {
    return uom;
  }

  public void setUom(Uom uom) {
    this.uom = uom;
  }

  public ProcessOutputType getOutputType() {
    return outputType;
  }

  public void setOutputType(ProcessOutputType outputType) {
    this.outputType = outputType;
  }

  public BigDecimal getDefaultRatio() {
    return defaultRatio;
  }

  public void setDefaultRatio(BigDecimal defaultRatio) {
    this.defaultRatio = defaultRatio;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }
}
