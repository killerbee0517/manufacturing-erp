package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import com.manufacturing.erp.domain.Enums.DocumentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.CascadeType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rfq")
public class Rfq extends BaseEntity {
  @Column(name = "rfq_no", nullable = false)
  private String rfqNo;

  @ManyToOne
  private Supplier supplier;

  private LocalDate rfqDate;

  private String paymentTerms;

  private String narration;

  private String closureReason;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DocumentStatus status;

  @OneToMany(mappedBy = "rfq", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RfqLine> lines = new ArrayList<>();

  public String getRfqNo() {
    return rfqNo;
  }

  public void setRfqNo(String rfqNo) {
    this.rfqNo = rfqNo;
  }

  public Supplier getSupplier() {
    return supplier;
  }

  public void setSupplier(Supplier supplier) {
    this.supplier = supplier;
  }

  public LocalDate getRfqDate() {
    return rfqDate;
  }

  public void setRfqDate(LocalDate rfqDate) {
    this.rfqDate = rfqDate;
  }

  public String getRemarks() {
    return narration;
  }

  public void setRemarks(String remarks) {
    this.narration = remarks;
  }

  public String getPaymentTerms() {
    return paymentTerms;
  }

  public void setPaymentTerms(String paymentTerms) {
    this.paymentTerms = paymentTerms;
  }

  public String getNarration() {
    return narration;
  }

  public void setNarration(String narration) {
    this.narration = narration;
  }

  public String getClosureReason() {
    return closureReason;
  }

  public void setClosureReason(String closureReason) {
    this.closureReason = closureReason;
  }

  public DocumentStatus getStatus() {
    return status;
  }

  public void setStatus(DocumentStatus status) {
    this.status = status;
  }

  public List<RfqLine> getLines() {
    return lines;
  }

  public void setLines(List<RfqLine> lines) {
    this.lines = lines;
  }
}
