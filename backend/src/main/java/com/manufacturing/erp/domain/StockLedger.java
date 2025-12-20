package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import com.manufacturing.erp.domain.Enums.LedgerTxnType;
import com.manufacturing.erp.domain.Enums.StockStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "stock_ledger")
public class StockLedger extends BaseEntity {
  @Column(nullable = false)
  private String docType;

  @Column(nullable = false)
  private Long docId;

  private Long docLineId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private LedgerTxnType txnType;

  @Column(nullable = false)
  private BigDecimal quantity;

  @Column(nullable = false)
  private BigDecimal weight;

  @ManyToOne
  private Uom uom;

  @ManyToOne
  private Item item;

  @ManyToOne
  private Location fromLocation;

  @ManyToOne
  private Location toLocation;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StockStatus status;

  @Column(nullable = false)
  private Instant postedAt;

  public String getDocType() {
    return docType;
  }

  public void setDocType(String docType) {
    this.docType = docType;
  }

  public Long getDocId() {
    return docId;
  }

  public void setDocId(Long docId) {
    this.docId = docId;
  }

  public Long getDocLineId() {
    return docLineId;
  }

  public void setDocLineId(Long docLineId) {
    this.docLineId = docLineId;
  }

  public LedgerTxnType getTxnType() {
    return txnType;
  }

  public void setTxnType(LedgerTxnType txnType) {
    this.txnType = txnType;
  }

  public BigDecimal getQuantity() {
    return quantity;
  }

  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
  }

  public BigDecimal getWeight() {
    return weight;
  }

  public void setWeight(BigDecimal weight) {
    this.weight = weight;
  }

  public Uom getUom() {
    return uom;
  }

  public void setUom(Uom uom) {
    this.uom = uom;
  }

  public Item getItem() {
    return item;
  }

  public void setItem(Item item) {
    this.item = item;
  }

  public Location getFromLocation() {
    return fromLocation;
  }

  public void setFromLocation(Location fromLocation) {
    this.fromLocation = fromLocation;
  }

  public Location getToLocation() {
    return toLocation;
  }

  public void setToLocation(Location toLocation) {
    this.toLocation = toLocation;
  }

  public StockStatus getStatus() {
    return status;
  }

  public void setStatus(StockStatus status) {
    this.status = status;
  }

  public Instant getPostedAt() {
    return postedAt;
  }

  public void setPostedAt(Instant postedAt) {
    this.postedAt = postedAt;
  }
}
