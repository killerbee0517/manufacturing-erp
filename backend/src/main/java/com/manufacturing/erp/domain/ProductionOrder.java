package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import com.manufacturing.erp.domain.Enums.ProductionStatus;
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
  @JoinColumn(name = "item_id")
  private Item item;

  @Column(name = "planned_qty", nullable = false)
  private BigDecimal plannedQty;

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

  public Item getItem() {
    return item;
  }

  public void setItem(Item item) {
    this.item = item;
  }

  public BigDecimal getPlannedQty() {
    return plannedQty;
  }

  public void setPlannedQty(BigDecimal plannedQty) {
    this.plannedQty = plannedQty;
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
