package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.RfqQuoteHeader;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RfqQuoteHeaderRepository extends JpaRepository<RfqQuoteHeader, Long> {
  Optional<RfqQuoteHeader> findByRfqIdAndSupplierId(Long rfqId, Long supplierId);

  List<RfqQuoteHeader> findByRfqId(Long rfqId);
}
