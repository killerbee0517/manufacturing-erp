package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import com.manufacturing.erp.domain.Enums.PayablePartyType;
import com.manufacturing.erp.domain.Enums.PaymentDirection;
import com.manufacturing.erp.domain.Enums.PaymentMode;
import com.manufacturing.erp.domain.Enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payment_vouchers")
public class PaymentVoucher extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "company_id", nullable = false)
  private Company company;

  @Column(name = "voucher_no", nullable = false)
  private String voucherNo;

  @Column(name = "voucher_date", nullable = false)
  private LocalDate voucherDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "party_type", nullable = false)
  private PayablePartyType partyType;

  @Column(name = "party_id", nullable = false)
  private Long partyId;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_direction", nullable = false)
  private PaymentDirection paymentDirection;

  @Enumerated(EnumType.STRING)
  @Column(name = "payment_mode", nullable = false)
  private PaymentMode paymentMode;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "bank_id")
  private Bank bank;

  @Column(nullable = false)
  private BigDecimal amount;

  private String narration;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PaymentStatus status;

  @Column(name = "cheque_number")
  private String chequeNumber;

  @Column(name = "cheque_date")
  private LocalDate chequeDate;

  @OneToMany(mappedBy = "paymentVoucher", orphanRemoval = true)
  private List<PaymentVoucherAllocation> allocations = new ArrayList<>();

  public Company getCompany() {
    return company;
  }

  public void setCompany(Company company) {
    this.company = company;
  }

  public String getVoucherNo() {
    return voucherNo;
  }

  public void setVoucherNo(String voucherNo) {
    this.voucherNo = voucherNo;
  }

  public LocalDate getVoucherDate() {
    return voucherDate;
  }

  public void setVoucherDate(LocalDate voucherDate) {
    this.voucherDate = voucherDate;
  }

  public PayablePartyType getPartyType() {
    return partyType;
  }

  public void setPartyType(PayablePartyType partyType) {
    this.partyType = partyType;
  }

  public Long getPartyId() {
    return partyId;
  }

  public void setPartyId(Long partyId) {
    this.partyId = partyId;
  }

  public PaymentDirection getPaymentDirection() {
    return paymentDirection;
  }

  public void setPaymentDirection(PaymentDirection paymentDirection) {
    this.paymentDirection = paymentDirection;
  }

  public PaymentMode getPaymentMode() {
    return paymentMode;
  }

  public void setPaymentMode(PaymentMode paymentMode) {
    this.paymentMode = paymentMode;
  }

  public Bank getBank() {
    return bank;
  }

  public void setBank(Bank bank) {
    this.bank = bank;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public String getNarration() {
    return narration;
  }

  public void setNarration(String narration) {
    this.narration = narration;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  public void setStatus(PaymentStatus status) {
    this.status = status;
  }

  public String getChequeNumber() {
    return chequeNumber;
  }

  public void setChequeNumber(String chequeNumber) {
    this.chequeNumber = chequeNumber;
  }

  public LocalDate getChequeDate() {
    return chequeDate;
  }

  public void setChequeDate(LocalDate chequeDate) {
    this.chequeDate = chequeDate;
  }

  public List<PaymentVoucherAllocation> getAllocations() {
    return allocations;
  }

  public void setAllocations(List<PaymentVoucherAllocation> allocations) {
    this.allocations = allocations;
  }
}
