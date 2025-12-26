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

  @Column(name = "qty_in", nullable = false)
  private BigDecimal qtyIn;

  @Column(name = "qty_out", nullable = false)
  private BigDecimal qtyOut;

  @Column
  private BigDecimal rate;

  @Column
  private BigDecimal amount;

  @ManyToOne
  private Uom uom;

  @ManyToOne
  private Item item;

  @ManyToOne
  private Location fromLocation;

  @ManyToOne
  private Location toLocation;

  @ManyToOne
  private Godown fromGodown;

  @ManyToOne
  private Godown toGodown;

  @ManyToOne
  private Godown godown;

  @Column(name = "batch_id")
  private Long batchId;

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

  public BigDecimal getQtyIn() {
    return qtyIn;
  }

  public void setQtyIn(BigDecimal qtyIn) {
    this.qtyIn = qtyIn;
  }

  public BigDecimal getQtyOut() {
    return qtyOut;
  }

  public void setQtyOut(BigDecimal qtyOut) {
    this.qtyOut = qtyOut;
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

  public Godown getFromGodown() {
    return fromGodown;
  }

  public void setFromGodown(Godown fromGodown) {
    this.fromGodown = fromGodown;
  }

  public Godown getToGodown() {
    return toGodown;
  }

  public void setToGodown(Godown toGodown) {
    this.toGodown = toGodown;
  }

  public Godown getGodown() {
    return godown;
  }

  public void setGodown(Godown godown) {
    this.godown = godown;
  }

  public Long getBatchId() {
    return batchId;
  }

  public void setBatchId(Long batchId) {
    this.batchId = batchId;
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
