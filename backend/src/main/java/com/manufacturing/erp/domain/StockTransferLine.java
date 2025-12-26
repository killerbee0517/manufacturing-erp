package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "stock_transfer_lines")
public class StockTransferLine extends BaseEntity {
  @ManyToOne
  private StockTransferHeader header;

  @ManyToOne
  private Item item;

  @ManyToOne
  private Uom uom;

  @Column(name = "qty", nullable = false)
  private BigDecimal quantity;

  public StockTransferHeader getHeader() {
    return header;
  }

  public void setHeader(StockTransferHeader header) {
    this.header = header;
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

  public BigDecimal getQuantity() {
    return quantity;
  }

  public void setQuantity(BigDecimal quantity) {
    this.quantity = quantity;
  }
}
