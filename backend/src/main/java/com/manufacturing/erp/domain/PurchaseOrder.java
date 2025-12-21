package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import com.manufacturing.erp.domain.Enums.DocumentStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder extends BaseEntity {
  @Column(name = "po_no", nullable = false)
  private String poNo;

  @ManyToOne
  private Supplier supplier;

  private LocalDate poDate;

  private String remarks;

  private BigDecimal totalAmount;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DocumentStatus status;

  @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PurchaseOrderLine> lines = new ArrayList<>();

  public String getPoNo() {
    return poNo;
  }

  public void setPoNo(String poNo) {
    this.poNo = poNo;
  }

  public Supplier getSupplier() {
    return supplier;
  }

  public void setSupplier(Supplier supplier) {
    this.supplier = supplier;
  }

  public LocalDate getPoDate() {
    return poDate;
  }

  public void setPoDate(LocalDate poDate) {
    this.poDate = poDate;
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(BigDecimal totalAmount) {
    this.totalAmount = totalAmount;
  }

  public DocumentStatus getStatus() {
    return status;
  }

  public void setStatus(DocumentStatus status) {
    this.status = status;
  }

  public List<PurchaseOrderLine> getLines() {
    return lines;
  }

  public void setLines(List<PurchaseOrderLine> lines) {
    this.lines = lines;
  }
}
