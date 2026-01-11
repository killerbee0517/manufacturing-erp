package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import com.manufacturing.erp.domain.Enums.DocumentStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stock_transfer_header")
public class StockTransferHeader extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @Column(name = "transfer_no", nullable = false)
  private String transferNo;

  @ManyToOne
  private Godown fromGodown;

  @ManyToOne
  private Godown toGodown;

  @Column(name = "transfer_date", nullable = false)
  private LocalDate transferDate;

  @Column
  private String narration;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DocumentStatus status = DocumentStatus.DRAFT;

  @OneToMany(mappedBy = "header", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<StockTransferLine> lines = new ArrayList<>();

  public String getTransferNo() {
    return transferNo;
  }

  public Company getCompany() {
    return company;
  }

  public void setCompany(Company company) {
    this.company = company;
  }

  public void setTransferNo(String transferNo) {
    this.transferNo = transferNo;
  }

  public Godown getFromGodown() {
    return fromGodown;
  }

  public void setFromGodown(Godown fromGodown) {
    this.fromGodown = fromGodown;
  }

  public Godown getToGodown() {
    return toGodown;
  }

  public void setToGodown(Godown toGodown) {
    this.toGodown = toGodown;
  }

  public LocalDate getTransferDate() {
    return transferDate;
  }

  public void setTransferDate(LocalDate transferDate) {
    this.transferDate = transferDate;
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

  public List<StockTransferLine> getLines() {
    return lines;
  }

  public void setLines(List<StockTransferLine> lines) {
    this.lines = lines;
  }
}
