package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import com.manufacturing.erp.domain.Enums.DocumentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.OneToMany;

@Entity
@Table(name = "purchase_invoices")
public class PurchaseInvoice extends BaseEntity {
  @Column(nullable = false)
  private String invoiceNo;

  @ManyToOne
  private Supplier supplier;

  @ManyToOne
  @JoinColumn(name = "po_id")
  private PurchaseOrder purchaseOrder;

  @ManyToOne
  private Grn grn;

  @Column(name = "supplier_invoice_no")
  private String supplierInvoiceNo;

  @Column(nullable = false)
  private LocalDate invoiceDate;

  private String narration;

  @Column(nullable = false)
  private BigDecimal totalAmount;

  @Column(nullable = false)
  private BigDecimal tdsAmount;

  @Column(nullable = false)
  private BigDecimal netPayable;

  @Column(nullable = false)
  private BigDecimal subtotal;

  @Column(nullable = false)
  private BigDecimal taxTotal;

  @Column(nullable = false)
  private BigDecimal roundOff;

  @Column(nullable = false)
  private BigDecimal grandTotal;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DocumentStatus status;

  @OneToMany(mappedBy = "purchaseInvoice", orphanRemoval = true)
  private List<PurchaseInvoiceLine> lines = new ArrayList<>();

  public String getInvoiceNo() {
    return invoiceNo;
  }

  public void setInvoiceNo(String invoiceNo) {
    this.invoiceNo = invoiceNo;
  }

  public Supplier getSupplier() {
    return supplier;
  }

  public void setSupplier(Supplier supplier) {
    this.supplier = supplier;
  }

  public PurchaseOrder getPurchaseOrder() {
    return purchaseOrder;
  }

  public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
    this.purchaseOrder = purchaseOrder;
  }

  public Grn getGrn() {
    return grn;
  }

  public void setGrn(Grn grn) {
    this.grn = grn;
  }

  public String getSupplierInvoiceNo() {
    return supplierInvoiceNo;
  }

  public void setSupplierInvoiceNo(String supplierInvoiceNo) {
    this.supplierInvoiceNo = supplierInvoiceNo;
  }

  public LocalDate getInvoiceDate() {
    return invoiceDate;
  }

  public void setInvoiceDate(LocalDate invoiceDate) {
    this.invoiceDate = invoiceDate;
  }

  public String getNarration() {
    return narration;
  }

  public void setNarration(String narration) {
    this.narration = narration;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(BigDecimal totalAmount) {
    this.totalAmount = totalAmount;
  }

  public BigDecimal getTdsAmount() {
    return tdsAmount;
  }

  public void setTdsAmount(BigDecimal tdsAmount) {
    this.tdsAmount = tdsAmount;
  }

  public BigDecimal getNetPayable() {
    return netPayable;
  }

  public void setNetPayable(BigDecimal netPayable) {
    this.netPayable = netPayable;
  }

  public BigDecimal getSubtotal() {
    return subtotal;
  }

  public void setSubtotal(BigDecimal subtotal) {
    this.subtotal = subtotal;
  }

  public BigDecimal getTaxTotal() {
    return taxTotal;
  }

  public void setTaxTotal(BigDecimal taxTotal) {
    this.taxTotal = taxTotal;
  }

  public BigDecimal getRoundOff() {
    return roundOff;
  }

  public void setRoundOff(BigDecimal roundOff) {
    this.roundOff = roundOff;
  }

  public BigDecimal getGrandTotal() {
    return grandTotal;
  }

  public void setGrandTotal(BigDecimal grandTotal) {
    this.grandTotal = grandTotal;
  }

  public DocumentStatus getStatus() {
    return status;
  }

  public void setStatus(DocumentStatus status) {
    this.status = status;
  }

  public List<PurchaseInvoiceLine> getLines() {
    return lines;
  }

  public void setLines(List<PurchaseInvoiceLine> lines) {
    this.lines = lines;
  }
}
