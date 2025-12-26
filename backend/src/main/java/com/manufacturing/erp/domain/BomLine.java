package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "bom_lines")
public class BomLine extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "bom_id", nullable = false)
  private BomHeader bom;

  @ManyToOne
  @JoinColumn(name = "component_item_id", nullable = false)
  private Item componentItem;

  @ManyToOne
  @JoinColumn(name = "uom_id", nullable = false)
  private Uom uom;

  @Column(name = "qty_per_unit", nullable = false)
  private BigDecimal qtyPerUnit;

  @Column(name = "scrap_percent")
  private BigDecimal scrapPercent;

  public BomHeader getBom() {
    return bom;
  }

  public void setBom(BomHeader bom) {
    this.bom = bom;
  }

  public Item getComponentItem() {
    return componentItem;
  }

  public void setComponentItem(Item componentItem) {
    this.componentItem = componentItem;
  }

  public Uom getUom() {
    return uom;
  }

  public void setUom(Uom uom) {
    this.uom = uom;
  }

  public BigDecimal getQtyPerUnit() {
    return qtyPerUnit;
  }

  public void setQtyPerUnit(BigDecimal qtyPerUnit) {
    this.qtyPerUnit = qtyPerUnit;
  }

  public BigDecimal getScrapPercent() {
    return scrapPercent;
  }

  public void setScrapPercent(BigDecimal scrapPercent) {
    this.scrapPercent = scrapPercent;
  }
}
