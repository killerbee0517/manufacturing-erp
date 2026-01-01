package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.Voucher;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
  Optional<Voucher> findFirstByReferenceTypeAndReferenceId(String referenceType, Long referenceId);
}
