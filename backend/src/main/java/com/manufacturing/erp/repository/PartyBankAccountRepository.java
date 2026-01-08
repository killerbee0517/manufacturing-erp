package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.PartyBankAccount;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartyBankAccountRepository extends JpaRepository<PartyBankAccount, Long> {
  List<PartyBankAccount> findByCompanyId(Long companyId);
  List<PartyBankAccount> findByCompanyIdAndPartyId(Long companyId, Long partyId);
}
