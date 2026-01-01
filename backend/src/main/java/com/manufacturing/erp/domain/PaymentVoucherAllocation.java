package com.manufacturing.erp.domain;

import com.manufacturing.erp.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "payment_voucher_allocations")
public class PaymentVoucherAllocation extends BaseEntity {
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "payment_voucher_id", nullable = false)
  private PaymentVoucher paymentVoucher;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "purchase_invoice_id")
  private PurchaseInvoice purchaseInvoice;

  @Column(name = "allocated_amount", nullable = false)
  private BigDecimal allocatedAmount;

  private String remarks;

  public PaymentVoucher getPaymentVoucher() {
    return paymentVoucher;
  }

  public void setPaymentVoucher(PaymentVoucher paymentVoucher) {
    this.paymentVoucher = paymentVoucher;
  }

  public PurchaseInvoice getPurchaseInvoice() {
    return purchaseInvoice;
  }

  public void setPurchaseInvoice(PurchaseInvoice purchaseInvoice) {
    this.purchaseInvoice = purchaseInvoice;
  }

  public BigDecimal getAllocatedAmount() {
    return allocatedAmount;
  }

  public void setAllocatedAmount(BigDecimal allocatedAmount) {
    this.allocatedAmount = allocatedAmount;
  }

  public String getRemarks() {
    return remarks;
  }

  public void setRemarks(String remarks) {
    this.remarks = remarks;
  }
}
