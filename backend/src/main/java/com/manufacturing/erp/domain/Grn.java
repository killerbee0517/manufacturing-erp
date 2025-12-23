package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import com.manufacturing.erp.domain.Enums.DocumentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "grn")
public class Grn extends BaseEntity {
  @Column(nullable = false)
  private String grnNo;

  @ManyToOne
  private Supplier supplier;

  @ManyToOne
  private WeighbridgeTicket weighbridgeTicket;

  @ManyToOne
  private PurchaseOrder purchaseOrder;

  @ManyToOne
  private Godown godown;

  @ManyToOne
  private Item item;

  @ManyToOne
  private Uom uom;

  @Column(nullable = false)
  private LocalDate grnDate;

  private String narration;

  private java.math.BigDecimal firstWeight;

  private java.math.BigDecimal secondWeight;

  private java.math.BigDecimal netWeight;

  private java.math.BigDecimal quantity;

  @OneToMany(mappedBy = "grn", orphanRemoval = true)
  private List<GrnLine> lines = new ArrayList<>();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DocumentStatus status;

  public String getGrnNo() {
    return grnNo;
  }

  public void setGrnNo(String grnNo) {
    this.grnNo = grnNo;
  }

  public Supplier getSupplier() {
    return supplier;
  }

  public void setSupplier(Supplier supplier) {
    this.supplier = supplier;
  }

  public WeighbridgeTicket getWeighbridgeTicket() {
    return weighbridgeTicket;
  }

  public void setWeighbridgeTicket(WeighbridgeTicket weighbridgeTicket) {
    this.weighbridgeTicket = weighbridgeTicket;
  }

  public PurchaseOrder getPurchaseOrder() {
    return purchaseOrder;
  }

  public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
    this.purchaseOrder = purchaseOrder;
  }

  public Godown getGodown() {
    return godown;
  }

  public void setGodown(Godown godown) {
    this.godown = godown;
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

  public LocalDate getGrnDate() {
    return grnDate;
  }

  public void setGrnDate(LocalDate grnDate) {
    this.grnDate = grnDate;
  }

  public String getNarration() {
    return narration;
  }

  public void setNarration(String narration) {
    this.narration = narration;
  }

  public java.math.BigDecimal getFirstWeight() {
    return firstWeight;
  }

  public void setFirstWeight(java.math.BigDecimal firstWeight) {
    this.firstWeight = firstWeight;
  }

  public java.math.BigDecimal getSecondWeight() {
    return secondWeight;
  }

  public void setSecondWeight(java.math.BigDecimal secondWeight) {
    this.secondWeight = secondWeight;
  }

  public java.math.BigDecimal getNetWeight() {
    return netWeight;
  }

  public void setNetWeight(java.math.BigDecimal netWeight) {
    this.netWeight = netWeight;
  }

  public java.math.BigDecimal getQuantity() {
    return quantity;
  }

  public void setQuantity(java.math.BigDecimal quantity) {
    this.quantity = quantity;
  }

  public List<GrnLine> getLines() {
    return lines;
  }

  public void setLines(List<GrnLine> lines) {
    this.lines = lines;
  }

  public DocumentStatus getStatus() {
    return status;
  }

  public void setStatus(DocumentStatus status) {
    this.status = status;
  }
}
