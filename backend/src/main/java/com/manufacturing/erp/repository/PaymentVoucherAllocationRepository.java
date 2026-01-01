package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.PaymentVoucherAllocation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentVoucherAllocationRepository extends JpaRepository<PaymentVoucherAllocation, Long> {
  List<PaymentVoucherAllocation> findByPaymentVoucherId(Long paymentVoucherId);
}
