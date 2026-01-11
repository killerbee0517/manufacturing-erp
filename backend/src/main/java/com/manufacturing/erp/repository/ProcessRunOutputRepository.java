package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.ProcessRunOutput;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProcessRunOutputRepository extends JpaRepository<ProcessRunOutput, Long> {
  List<ProcessRunOutput> findByProcessRunId(Long processRunId);

  List<ProcessRunOutput> findByProcessRunProductionBatchId(Long batchId);

  List<ProcessRunOutput> findByProcessRunCompanyId(Long companyId);

  @Query("select coalesce(sum(o.quantity), 0) from ProcessRunOutput o "
      + "where o.processRun.company.id = :companyId and o.processRun.runDate = :runDate")
  BigDecimal sumQuantityByCompanyIdAndRunDate(@Param("companyId") Long companyId,
                                              @Param("runDate") LocalDate runDate);

  @Query("select coalesce(sum(o.quantity), 0) from ProcessRunOutput o "
      + "where o.processRun.company.id = :companyId and o.processRun.runDate between :startDate and :endDate")
  BigDecimal sumQuantityByCompanyIdAndRunDateBetween(@Param("companyId") Long companyId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);
}
