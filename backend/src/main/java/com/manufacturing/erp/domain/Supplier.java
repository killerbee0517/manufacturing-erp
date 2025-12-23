package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "suppliers")
public class Supplier extends BaseEntity {
  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String code;

  @Column
  private String pan;

  @Column
  private String address;

  @Column
  private String state;

  @Column
  private String country;

  @Column(name = "pin_code")
  private String pinCode;

  @Column(name = "gst_no")
  private String gstNo;

  @Column
  private String contact;

  @Column
  private String email;

  @ManyToOne
  @JoinColumn(name = "bank_id")
  private Bank bank;

  @Column(name = "supplier_type")
  private String supplierType;

  @Column(name = "credit_period")
  private Integer creditPeriod;

  @ManyToOne
  @JoinColumn(name = "ledger_id")
  private Ledger ledger;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getPan() {
    return pan;
  }

  public void setPan(String pan) {
    this.pan = pan;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getPinCode() {
    return pinCode;
  }

  public void setPinCode(String pinCode) {
    this.pinCode = pinCode;
  }

  public String getGstNo() {
    return gstNo;
  }

  public void setGstNo(String gstNo) {
    this.gstNo = gstNo;
  }

  public String getContact() {
    return contact;
  }

  public void setContact(String contact) {
    this.contact = contact;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Bank getBank() {
    return bank;
  }

  public void setBank(Bank bank) {
    this.bank = bank;
  }

  public String getSupplierType() {
    return supplierType;
  }

  public void setSupplierType(String supplierType) {
    this.supplierType = supplierType;
  }

  public Integer getCreditPeriod() {
    return creditPeriod;
  }

  public void setCreditPeriod(Integer creditPeriod) {
    this.creditPeriod = creditPeriod;
  }

  public Ledger getLedger() {
    return ledger;
  }

  public void setLedger(Ledger ledger) {
    this.ledger = ledger;
  }
}
