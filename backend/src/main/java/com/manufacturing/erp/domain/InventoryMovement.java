package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import com.manufacturing.erp.domain.Enums.InventoryLocationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "inventory_movements")
public class InventoryMovement extends BaseEntity {
  @Column(name = "txn_type", nullable = false)
  private String txnType;

  @Column(name = "ref_type")
  private String refType;

  @Column(name = "ref_id")
  private Long refId;

  @ManyToOne
  @JoinColumn(name = "item_id", nullable = false)
  private Item item;

  @Column(name = "qty_in", nullable = false)
  private BigDecimal qtyIn = BigDecimal.ZERO;

  @Column(name = "qty_out", nullable = false)
  private BigDecimal qtyOut = BigDecimal.ZERO;

  @ManyToOne
  @JoinColumn(name = "uom_id", nullable = false)
  private Uom uom;

  @Enumerated(EnumType.STRING)
  @Column(name = "location_type", nullable = false)
  private InventoryLocationType locationType;

  @Column(name = "location_id")
  private Long locationId;

  public String getTxnType() {
    return txnType;
  }

  public void setTxnType(String txnType) {
    this.txnType = txnType;
  }

  public String getRefType() {
    return refType;
  }

  public void setRefType(String refType) {
    this.refType = refType;
  }

  public Long getRefId() {
    return refId;
  }

  public void setRefId(Long refId) {
    this.refId = refId;
  }

  public Item getItem() {
    return item;
  }

  public void setItem(Item item) {
    this.item = item;
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

  public Uom getUom() {
    return uom;
  }

  public void setUom(Uom uom) {
    this.uom = uom;
  }

  public InventoryLocationType getLocationType() {
    return locationType;
  }

  public void setLocationType(InventoryLocationType locationType) {
    this.locationType = locationType;
  }

  public Long getLocationId() {
    return locationId;
  }

  public void setLocationId(Long locationId) {
    this.locationId = locationId;
  }
}
