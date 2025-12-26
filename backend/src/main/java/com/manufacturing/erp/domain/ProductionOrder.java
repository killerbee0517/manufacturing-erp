package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import com.manufacturing.erp.domain.Enums.ProductionStatus;
import com.manufacturing.erp.domain.BomHeader;
import com.manufacturing.erp.domain.Item;
import com.manufacturing.erp.domain.Uom;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "production_orders")
public class ProductionOrder extends BaseEntity {
  @Column(name = "order_no", nullable = false)
  private String orderNo;

  @ManyToOne
  @JoinColumn(name = "template_id")
  private ProcessTemplate template;

  @ManyToOne
  @JoinColumn(name = "bom_id")
  private BomHeader bom;

  @ManyToOne
  @JoinColumn(name = "finished_item_id")
  private Item finishedItem;

  @Column(name = "planned_qty", nullable = false)
  private BigDecimal plannedQty;

  @ManyToOne
  @JoinColumn(name = "uom_id")
  private Uom uom;

  @Column(name = "order_date")
  private LocalDate orderDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ProductionStatus status;

  public String getOrderNo() {
    return orderNo;
  }

  public void setOrderNo(String orderNo) {
    this.orderNo = orderNo;
  }

  public ProcessTemplate getTemplate() {
    return template;
  }

  public void setTemplate(ProcessTemplate template) {
    this.template = template;
  }

  public BomHeader getBom() {
    return bom;
  }

  public void setBom(BomHeader bom) {
    this.bom = bom;
  }

  public Item getFinishedItem() {
    return finishedItem;
  }

  public void setFinishedItem(Item finishedItem) {
    this.finishedItem = finishedItem;
  }

  public BigDecimal getPlannedQty() {
    return plannedQty;
  }

  public void setPlannedQty(BigDecimal plannedQty) {
    this.plannedQty = plannedQty;
  }

  public Uom getUom() {
    return uom;
  }

  public void setUom(Uom uom) {
    this.uom = uom;
  }

  public LocalDate getOrderDate() {
    return orderDate;
  }

  public void setOrderDate(LocalDate orderDate) {
    this.orderDate = orderDate;
  }

  public ProductionStatus getStatus() {
    return status;
  }

  public void setStatus(ProductionStatus status) {
    this.status = status;
  }
}
