package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.RfqQuoteLine;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RfqQuoteLineRepository extends JpaRepository<RfqQuoteLine, Long> {
  List<RfqQuoteLine> findByQuoteHeaderId(Long quoteHeaderId);
}
