package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import com.manufacturing.erp.domain.Enums.DocumentStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rfq_quote_header")
public class RfqQuoteHeader extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "company_id")
  private Company company;

  @ManyToOne
  @JoinColumn(name = "rfq_id")
  private Rfq rfq;

  @ManyToOne
  @JoinColumn(name = "supplier_id")
  private Supplier supplier;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DocumentStatus status;

  @Column(name = "payment_terms_override")
  private String paymentTermsOverride;

  private String remarks;

  @Column(name = "submitted_at")
  private Instant submittedAt;

  @OneToMany(mappedBy = "quoteHeader", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RfqQuoteLine> lines = new ArrayList<>();

  public Rfq getRfq() {
    return rfq;
  }

  public void setRfq(Rfq rfq) {
    this.rfq = rfq;
  }

  public Company getCompany() {
    return company;
  }

  public void setCompany(Company company) {
    this.company = company;
  }

  public Supplier getSupplier() {
    return supplier;
  }

  public void setSupplier(Supplier supplier) {
    this.supplier = supplier;
  }

  public DocumentStatus getStatus() {
    return status;
  }

  public void setStatus(DocumentStatus status) {
    this.status = status;
  }

  public String getPaymentTermsOverride() {
    return paymentTermsOverride;
  }

  public void setPaymentTermsOverride(String paymentTermsOverride) {
    this.paymentTermsOverride = paymentTermsOverride;
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }

  public Instant getSubmittedAt() {
    return submittedAt;
  }

  public void setSubmittedAt(Instant submittedAt) {
    this.submittedAt = submittedAt;
  }

  public List<RfqQuoteLine> getLines() {
    return lines;
  }

  public void setLines(List<RfqQuoteLine> lines) {
    this.lines = lines;
  }
}
