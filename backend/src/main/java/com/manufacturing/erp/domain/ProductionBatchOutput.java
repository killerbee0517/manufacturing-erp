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
import java.time.Instant;

@Entity
@Table(name = "production_batch_outputs")
public class ProductionBatchOutput extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "batch_id", nullable = false)
  private ProductionBatch batch;

  @ManyToOne
  @JoinColumn(name = "item_id", nullable = false)
  private Item item;

  @ManyToOne
  @JoinColumn(name = "uom_id", nullable = false)
  private Uom uom;

  @Column(name = "produced_qty", nullable = false)
  private BigDecimal producedQty;

  @Column(name = "consumed_qty", nullable = false)
  private BigDecimal consumedQty = BigDecimal.ZERO;

  @Enumerated(EnumType.STRING)
  @Column(name = "output_type", nullable = false)
  private ProcessOutputType outputType;

  @ManyToOne
  @JoinColumn(name = "destination_godown_id")
  private Godown destinationGodown;

  @Column(name = "produced_at")
  private Instant producedAt;

  public ProductionBatch getBatch() {
    return batch;
  }

  public void setBatch(ProductionBatch batch) {
    this.batch = batch;
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

  public BigDecimal getProducedQty() {
    return producedQty;
  }

  public void setProducedQty(BigDecimal producedQty) {
    this.producedQty = producedQty;
  }

  public BigDecimal getConsumedQty() {
    return consumedQty;
  }

  public void setConsumedQty(BigDecimal consumedQty) {
    this.consumedQty = consumedQty;
  }

  public ProcessOutputType getOutputType() {
    return outputType;
  }

  public void setOutputType(ProcessOutputType outputType) {
    this.outputType = outputType;
  }

  public Godown getDestinationGodown() {
    return destinationGodown;
  }

  public void setDestinationGodown(Godown destinationGodown) {
    this.destinationGodown = destinationGodown;
  }

  public Instant getProducedAt() {
    return producedAt;
  }

  public void setProducedAt(Instant producedAt) {
    this.producedAt = producedAt;
  }
}
