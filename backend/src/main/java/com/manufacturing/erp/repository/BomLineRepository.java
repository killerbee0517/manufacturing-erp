package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.BomLine;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BomLineRepository extends JpaRepository<BomLine, Long> {
  List<BomLine> findByBomId(Long bomId);
}
