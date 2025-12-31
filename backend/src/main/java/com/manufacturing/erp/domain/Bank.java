package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "banks")
public class Bank extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id")
  private Company company;

  @Column(nullable = false)
  private String name;

  @Column
  private String branch;

  @Column(name = "acc_no")
  private String accNo;

  @Column
  private String ifsc;

  @Column(name = "swift_code")
  private String swiftCode;

  @Column
  private String type;

  @Column(nullable = false)
  private boolean active = true;

  public Company getCompany() {
    return company;
  }

  public void setCompany(Company company) {
    this.company = company;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getBranch() {
    return branch;
  }

  public void setBranch(String branch) {
    this.branch = branch;
  }

  public String getAccNo() {
    return accNo;
  }

  public void setAccNo(String accNo) {
    this.accNo = accNo;
  }

  public String getIfsc() {
    return ifsc;
  }

  public void setIfsc(String ifsc) {
    this.ifsc = ifsc;
  }

  public String getSwiftCode() {
    return swiftCode;
  }

  public void setSwiftCode(String swiftCode) {
    this.swiftCode = swiftCode;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }
}
