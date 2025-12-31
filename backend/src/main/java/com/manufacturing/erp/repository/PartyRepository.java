package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.Party;
import com.manufacturing.erp.domain.Enums.PartyRoleType;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PartyRepository extends JpaRepository<Party, Long> {
  @Query("""
      select p from Party p
      where p.company.id = :companyId
        and (:search is null or lower(p.name) like lower(concat('%', :search, '%')) or lower(p.partyCode) like lower(concat('%', :search, '%')))
      """)
  Page<Party> search(@Param("companyId") Long companyId, @Param("search") String search, Pageable pageable);

  @Query("""
      select distinct p from Party p
      join PartyRole r on r.party.id = p.id and r.company.id = p.company.id
      where p.company.id = :companyId
        and r.roleType = :role
        and (:search is null or lower(p.name) like lower(concat('%', :search, '%')) or lower(p.partyCode) like lower(concat('%', :search, '%')))
      """)
  Page<Party> searchByRole(@Param("companyId") Long companyId, @Param("role") PartyRoleType role, @Param("search") String search, Pageable pageable);

  @Query("""
      select distinct p from Party p
      join PartyRole r on r.party.id = p.id and r.company.id = p.company.id
      where p.company.id = :companyId
        and r.roleType = :role
        and (:search is null or lower(p.name) like lower(concat('%', :search, '%')) or lower(p.partyCode) like lower(concat('%', :search, '%')))
      """)
  List<Party> autocompleteByRole(@Param("companyId") Long companyId, @Param("role") PartyRoleType role, @Param("search") String search, Pageable pageable);

  @Query("""
      select p from Party p
      where p.company.id = :companyId
        and (:search is null or lower(p.name) like lower(concat('%', :search, '%')) or lower(p.partyCode) like lower(concat('%', :search, '%')))
      """)
  List<Party> autocomplete(@Param("companyId") Long companyId, @Param("search") String search, Pageable pageable);

  long countByCompanyId(Long companyId);
}
