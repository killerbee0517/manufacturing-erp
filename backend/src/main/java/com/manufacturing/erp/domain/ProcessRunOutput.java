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
@Table(name = "process_run_outputs")
public class ProcessRunOutput extends BaseEntity {
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
  @JoinColumn(name = "dest_godown_id")
  private Godown destGodown;

  @Column(nullable = false)
  private BigDecimal quantity;

  @Enumerated(EnumType.STRING)
  @Column(name = "output_type", nullable = false)
  private ProcessOutputType outputType;

  @Column
  private BigDecimal rate;

  @Column
  private BigDecimal amount;

  @Column(name = "consumed_qty")
  private BigDecimal consumedQuantity = BigDecimal.ZERO;

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

  public Godown getDestGodown() {
    return destGodown;
  }

  public void setDestGodown(Godown destGodown) {
    this.destGodown = destGodown;
  }

  public BigDecimal getQuantity() {
    return quantity;
  }

  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
  }

  public ProcessOutputType getOutputType() {
    return outputType;
  }

  public void setOutputType(ProcessOutputType outputType) {
    this.outputType = outputType;
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

  public BigDecimal getConsumedQuantity() {
    return consumedQuantity;
  }

  public void setConsumedQuantity(BigDecimal consumedQuantity) {
    this.consumedQuantity = consumedQuantity;
  }
}
