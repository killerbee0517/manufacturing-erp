package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.ExpenseParty;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpensePartyRepository extends JpaRepository<ExpenseParty, Long> {
  Optional<ExpenseParty> findByPartyId(Long partyId);
}
