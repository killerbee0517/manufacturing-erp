package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.Enums.LedgerType;
import com.manufacturing.erp.domain.Ledger;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LedgerRepository extends JpaRepository<Ledger, Long> {
  Optional<Ledger> findByNameAndType(String name, LedgerType type);

  @Query("select l from Ledger l where (:type is null or l.type = :type) "
      + "and (:q is null or lower(l.name) like lower(concat('%', :q, '%'))) "
      + "order by l.name asc")
  List<Ledger> search(@Param("q") String q, @Param("type") LedgerType type);
}
