package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.Enums.LedgerType;
import com.manufacturing.erp.domain.Ledger;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerRepository extends JpaRepository<Ledger, Long> {
  Optional<Ledger> findByNameAndType(String name, LedgerType type);
}
