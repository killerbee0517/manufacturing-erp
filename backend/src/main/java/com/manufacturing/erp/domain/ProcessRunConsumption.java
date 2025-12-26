package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import com.manufacturing.erp.domain.Enums.ProcessInputSourceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "process_run_consumptions")
public class ProcessRunConsumption extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "process_run_id", nullable = false)
  private ProcessRun processRun;

  @ManyToOne
  @JoinColumn(name = "item_id", nullable = false)
  private Item item;

  @ManyToOne
  @JoinColumn(name = "uom_id", nullable = false)
  private Uom uom;

  @ManyToOne
  @JoinColumn(name = "source_godown_id")
  private Godown sourceGodown;

  @ManyToOne
  @JoinColumn(name = "source_run_output_id")
  private ProcessRunOutput sourceRunOutput;

  @Column(nullable = false)
  private BigDecimal quantity;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_type", nullable = false)
  private ProcessInputSourceType sourceType;

  @Column
  private BigDecimal rate;

  @Column
  private BigDecimal amount;

  public ProcessRun getProcessRun() {
    return processRun;
  }

  public void setProcessRun(ProcessRun processRun) {
    this.processRun = processRun;
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

  public Godown getSourceGodown() {
    return sourceGodown;
  }

  public void setSourceGodown(Godown sourceGodown) {
    this.sourceGodown = sourceGodown;
  }

  public ProcessRunOutput getSourceRunOutput() {
    return sourceRunOutput;
  }

  public void setSourceRunOutput(ProcessRunOutput sourceRunOutput) {
    this.sourceRunOutput = sourceRunOutput;
  }

  public ProcessInputSourceType getSourceType() {
    return sourceType;
  }

  public void setSourceType(ProcessInputSourceType sourceType) {
    this.sourceType = sourceType;
  }

  public BigDecimal getQuantity() {
    return quantity;
  }

  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
  }

  public BigDecimal getRate() {
    return rate;
  }

  public void setRate(BigDecimal rate) {
    this.rate = rate;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }
}
