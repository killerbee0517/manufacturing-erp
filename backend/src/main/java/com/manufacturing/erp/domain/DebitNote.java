package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import com.manufacturing.erp.domain.Enums.DebitNoteReason;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "debit_notes")
public class DebitNote extends BaseEntity {
  @Column(name = "debit_note_no", nullable = false)
  private String debitNoteNo;

  @ManyToOne
  private Supplier supplier;

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

  public DebitNoteReason getReason() {
    return reason;
  }

  public void setReason(DebitNoteReason reason) {
    this.reason = reason;
  }
}
