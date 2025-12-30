package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import com.manufacturing.erp.domain.Enums.DebitNoteReason;
import com.manufacturing.erp.domain.Enums.DocumentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "debit_notes")
public class DebitNote extends BaseEntity {
  @Column(name = "debit_note_no", nullable = false)
  private String debitNoteNo;

  @ManyToOne
  private Supplier supplier;

  @ManyToOne
  private PurchaseInvoice purchaseInvoice;

  @ManyToOne
  @JoinColumn(name = "po_id")
  private PurchaseOrder purchaseOrder;

  @ManyToOne
  private Grn grn;

  private LocalDate dnDate;

  private String narration;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DocumentStatus status;

  @Column(name = "total_deduction", nullable = false)
  private BigDecimal totalDeduction = BigDecimal.ZERO;

  @OneToMany(mappedBy = "debitNote", orphanRemoval = true)
  private List<DebitNoteLine> lines = new ArrayList<>();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DebitNoteReason reason;

  public String getDebitNoteNo() {
    return debitNoteNo;
  }

  public void setDebitNoteNo(String debitNoteNo) {
    this.debitNoteNo = debitNoteNo;
  }

  public Supplier getSupplier() {
    return supplier;
  }

  public void setSupplier(Supplier supplier) {
    this.supplier = supplier;
  }

  public PurchaseInvoice getPurchaseInvoice() {
    return purchaseInvoice;
  }

  public void setPurchaseInvoice(PurchaseInvoice purchaseInvoice) {
    this.purchaseInvoice = purchaseInvoice;
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

  public LocalDate getDnDate() {
    return dnDate;
  }

  public void setDnDate(LocalDate dnDate) {
    this.dnDate = dnDate;
  }

  public String getNarration() {
    return narration;
  }

  public void setNarration(String narration) {
    this.narration = narration;
  }

  public DocumentStatus getStatus() {
    return status;
  }

  public void setStatus(DocumentStatus status) {
    this.status = status;
  }

  public BigDecimal getTotalDeduction() {
    return totalDeduction;
  }

  public void setTotalDeduction(BigDecimal totalDeduction) {
    this.totalDeduction = totalDeduction;
  }

  public List<DebitNoteLine> getLines() {
    return lines;
  }

  public void setLines(List<DebitNoteLine> lines) {
    this.lines = lines;
  }

  public DebitNoteReason getReason() {
    return reason;
  }

  public void setReason(DebitNoteReason reason) {
    this.reason = reason;
  }
}
