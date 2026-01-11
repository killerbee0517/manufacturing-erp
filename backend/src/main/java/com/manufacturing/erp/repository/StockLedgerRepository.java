package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.StockLedger;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StockLedgerRepository extends JpaRepository<StockLedger, Long> {
  List<StockLedger> findByDocType(String docType);

  @Query("SELECT l FROM StockLedger l WHERE l.company.id = :companyId "
      + "AND (:itemId IS NULL OR l.item.id = :itemId) "
      + "AND (:godownId IS NULL OR l.godown.id = :godownId) "
      + "AND (:from IS NULL OR l.postedAt >= :from) AND (:to IS NULL OR l.postedAt <= :to) "
      + "ORDER BY l.postedAt ASC")
  List<StockLedger> findLedger(@Param("companyId") Long companyId,
                               @Param("itemId") Long itemId,
                               @Param("godownId") Long godownId,
                               @Param("from") Instant from,
                               @Param("to") Instant to);

  long countByCompanyId(Long companyId);

  @Query("select coalesce(sum(l.amount), 0) from StockLedger l where l.company.id = :companyId")
  java.math.BigDecimal sumAmountByCompanyId(@Param("companyId") Long companyId);
}
