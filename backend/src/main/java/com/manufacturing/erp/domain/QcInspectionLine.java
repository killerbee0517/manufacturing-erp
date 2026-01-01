package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "qc_inspection_lines")
public class QcInspectionLine extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "qc_inspection_id")
  private QcInspection qcInspection;

  @ManyToOne
  @JoinColumn(name = "purchase_order_line_id")
  private PurchaseOrderLine purchaseOrderLine;

  @ManyToOne
  @JoinColumn(name = "grn_line_id")
  private GrnLine grnLine;

  @Column(name = "received_qty")
  private BigDecimal receivedQty;

  @Column(name = "accepted_qty")
  private BigDecimal acceptedQty;

  @Column(name = "rejected_qty")
  private BigDecimal rejectedQty;

  private String reason;

  public QcInspection getQcInspection() {
    return qcInspection;
  }

  public void setQcInspection(QcInspection qcInspection) {
    this.qcInspection = qcInspection;
  }

  public PurchaseOrderLine getPurchaseOrderLine() {
    return purchaseOrderLine;
  }

  public void setPurchaseOrderLine(PurchaseOrderLine purchaseOrderLine) {
    this.purchaseOrderLine = purchaseOrderLine;
  }

  public GrnLine getGrnLine() {
    return grnLine;
  }

  public void setGrnLine(GrnLine grnLine) {
    this.grnLine = grnLine;
  }

  public BigDecimal getReceivedQty() {
    return receivedQty;
  }

  public void setReceivedQty(BigDecimal receivedQty) {
    this.receivedQty = receivedQty;
  }

  public BigDecimal getAcceptedQty() {
    return acceptedQty;
  }

  public void setAcceptedQty(BigDecimal acceptedQty) {
    this.acceptedQty = acceptedQty;
  }

  public BigDecimal getRejectedQty() {
    return rejectedQty;
  }

  public void setRejectedQty(BigDecimal rejectedQty) {
    this.rejectedQty = rejectedQty;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }
}
