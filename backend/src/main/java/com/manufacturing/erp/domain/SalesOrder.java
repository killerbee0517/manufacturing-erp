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
@Table(name = "sales_orders")
public class SalesOrder extends BaseEntity {
  @ManyToOne
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @Column(name = "so_no", nullable = false)
  private String soNo;

  @ManyToOne
  private Customer customer;

  @Column(name = "order_date")
  private LocalDate orderDate;

  @Column
  private String narration;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DocumentStatus status = DocumentStatus.DRAFT;

  @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private List<SalesOrderLine> lines = new ArrayList<>();

  public String getSoNo() {
    return soNo;
  }

  public Company getCompany() {
    return company;
  }

  public void setCompany(Company company) {
    this.company = company;
  }

  public void setSoNo(String soNo) {
    this.soNo = soNo;
  }

  public Customer getCustomer() {
    return customer;
  }

  public void setCustomer(Customer customer) {
    this.customer = customer;
  }

  public LocalDate getOrderDate() {
    return orderDate;
  }

  public void setOrderDate(LocalDate orderDate) {
    this.orderDate = orderDate;
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

  public List<SalesOrderLine> getLines() {
    return lines;
  }

  public void setLines(List<SalesOrderLine> lines) {
    this.lines = lines;
  }
}
