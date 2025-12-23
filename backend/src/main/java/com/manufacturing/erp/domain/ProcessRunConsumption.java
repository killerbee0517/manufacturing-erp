package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
  @JoinColumn(name = "godown_id")
  private Godown godown;

  @Column(nullable = false)
  private BigDecimal quantity;

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

  public Godown getGodown() {
    return godown;
  }

  public void setGodown(Godown godown) {
    this.godown = godown;
  }

  public BigDecimal getQuantity() {
    return quantity;
  }

  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
  }
}
