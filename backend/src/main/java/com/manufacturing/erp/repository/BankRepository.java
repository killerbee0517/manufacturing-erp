package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.Bank;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BankRepository extends JpaRepository<Bank, Long> {
  List<Bank> findByNameContainingIgnoreCase(String name);

  @Query("""
      select b from Bank b
      where b.company.id = :companyId
        and (:search is null or lower(b.name) like lower(concat('%', :search, '%')) or lower(b.accNo) like lower(concat('%', :search, '%')))
      """)
  Page<Bank> search(@Param("companyId") Long companyId, @Param("search") String search, Pageable pageable);

  @Query("""
      select b from Bank b
      where b.company.id = :companyId
        and b.active = true
        and (:search is null or lower(b.name) like lower(concat('%', :search, '%')) or lower(b.accNo) like lower(concat('%', :search, '%')))
      """)
  List<Bank> autocomplete(@Param("companyId") Long companyId, @Param("search") String search, Pageable pageable);
}
