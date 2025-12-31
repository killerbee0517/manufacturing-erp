package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.PartyRole;
import com.manufacturing.erp.domain.Enums.PartyRoleType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartyRoleRepository extends JpaRepository<PartyRole, Long> {
  List<PartyRole> findByCompanyIdAndPartyId(Long companyId, Long partyId);

  Optional<PartyRole> findByCompanyIdAndPartyIdAndRoleType(Long companyId, Long partyId, PartyRoleType roleType);
}
