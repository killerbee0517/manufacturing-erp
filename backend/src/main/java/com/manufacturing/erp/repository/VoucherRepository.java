package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
}
