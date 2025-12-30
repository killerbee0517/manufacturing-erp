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
import java.time.Instant;

@Entity
@Table(name = "production_batch_inputs")
public class ProductionBatchInput extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "batch_id", nullable = false)
  private ProductionBatch batch;

  @ManyToOne
  @JoinColumn(name = "item_id", nullable = false)
  private Item item;

  @ManyToOne
  @JoinColumn(name = "uom_id", nullable = false)
  private Uom uom;

  @Column(name = "issued_qty", nullable = false)
  private BigDecimal issuedQty;

  @Enumerated(EnumType.STRING)
  @Column(name = "source_type", nullable = false)
  private ProcessInputSourceType sourceType;

  @Column(name = "source_ref_id")
  private Long sourceRefId;

  @ManyToOne
  @JoinColumn(name = "source_godown_id")
  private Godown sourceGodown;

  @Column(name = "issued_at")
  private Instant issuedAt;

  @Column(name = "step_no")
  private Integer stepNo;

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

  public BigDecimal getIssuedQty() {
    return issuedQty;
  }

  public void setIssuedQty(BigDecimal issuedQty) {
    this.issuedQty = issuedQty;
  }

  public ProcessInputSourceType getSourceType() {
    return sourceType;
  }

  public void setSourceType(ProcessInputSourceType sourceType) {
    this.sourceType = sourceType;
  }

  public Long getSourceRefId() {
    return sourceRefId;
  }

  public void setSourceRefId(Long sourceRefId) {
    this.sourceRefId = sourceRefId;
  }

  public Godown getSourceGodown() {
    return sourceGodown;
  }

  public void setSourceGodown(Godown sourceGodown) {
    this.sourceGodown = sourceGodown;
  }

  public Instant getIssuedAt() {
    return issuedAt;
  }

  public void setIssuedAt(Instant issuedAt) {
    this.issuedAt = issuedAt;
  }

  public Integer getStepNo() {
    return stepNo;
  }

  public void setStepNo(Integer stepNo) {
    this.stepNo = stepNo;
  }
}
