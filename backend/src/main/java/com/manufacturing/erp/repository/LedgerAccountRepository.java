package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.LedgerAccount;
import com.manufacturing.erp.domain.Enums.LedgerType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerAccountRepository extends JpaRepository<LedgerAccount, Long> {
  Optional<LedgerAccount> findByCompanyIdAndLedgerTypeAndPartyId(Long companyId, LedgerType ledgerType, Long partyId);

  Optional<LedgerAccount> findByCompanyIdAndLedgerTypeAndBankId(Long companyId, LedgerType ledgerType, Long bankId);

  List<LedgerAccount> findByCompanyId(Long companyId);
}
