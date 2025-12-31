package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.LedgerEntry;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
  @Query("""
      select coalesce(sum(le.debit) - sum(le.credit), 0) from LedgerEntry le
      where le.company.id = :companyId and le.ledgerAccount.id = :ledgerAccountId
      """)
  BigDecimal getBalance(@Param("companyId") Long companyId, @Param("ledgerAccountId") Long ledgerAccountId);

  Page<LedgerEntry> findByCompanyIdAndLedgerAccountIdOrderByTxnDateAsc(Long companyId, Long ledgerAccountId, Pageable pageable);

  Page<LedgerEntry> findByCompanyIdAndLedgerAccountIdAndTxnDateBetweenOrderByTxnDateAsc(Long companyId, Long ledgerAccountId, LocalDate from, LocalDate to, Pageable pageable);
}
