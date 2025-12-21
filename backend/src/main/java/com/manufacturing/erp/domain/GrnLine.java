package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "grn_lines")
public class GrnLine extends BaseEntity {
  @ManyToOne
  private Grn grn;

  @ManyToOne
  private Item item;

  @ManyToOne
  private Uom uom;

  @Column(nullable = false)
  private String bagType;

  @Column(nullable = false)
  private Integer bagCount;

  @Column(nullable = false)
  private BigDecimal quantity;

  @Column(nullable = false)
  private BigDecimal weight;

  public Grn getGrn() {
    return grn;
  }

  public void setGrn(Grn grn) {
    this.grn = grn;
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

  public String getBagType() {
    return bagType;
  }

  public void setBagType(String bagType) {
    this.bagType = bagType;
  }

  public Integer getBagCount() {
    return bagCount;
  }

  public void setBagCount(Integer bagCount) {
    this.bagCount = bagCount;
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
}
