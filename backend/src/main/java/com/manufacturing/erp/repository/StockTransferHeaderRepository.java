package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.StockTransferHeader;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockTransferHeaderRepository extends JpaRepository<StockTransferHeader, Long> {
  List<StockTransferHeader> findByCompanyId(Long companyId);

  Optional<StockTransferHeader> findByIdAndCompanyId(Long id, Long companyId);
}
