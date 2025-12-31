package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.PdcRegister;
import com.manufacturing.erp.domain.Enums.PdcStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PdcRegisterRepository extends JpaRepository<PdcRegister, Long> {
  @Query("""
      select p from PdcRegister p
      where p.company.id = :companyId
        and (:search is null or lower(p.pdcNo) like lower(concat('%', :search, '%')) or lower(p.chequeNumber) like lower(concat('%', :search, '%')))
        and (:status is null or p.status = :status)
      """)
  Page<PdcRegister> search(@Param("companyId") Long companyId, @Param("search") String search, @Param("status") PdcStatus status, Pageable pageable);
}
