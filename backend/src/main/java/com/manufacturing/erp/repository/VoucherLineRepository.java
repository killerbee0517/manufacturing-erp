package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.VoucherLine;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VoucherLineRepository extends JpaRepository<VoucherLine, Long> {
  @Query("select coalesce(sum(vl.drAmount),0) - coalesce(sum(vl.crAmount),0) from VoucherLine vl where vl.ledger.id = :ledgerId")
  BigDecimal findBalanceForLedger(@Param("ledgerId") Long ledgerId);

  @Query("select coalesce(sum(vl.drAmount),0) - coalesce(sum(vl.crAmount),0) from VoucherLine vl where vl.ledger.id = :ledgerId and vl.voucher.voucherDate < :fromDate")
  BigDecimal findOpeningBalance(@Param("ledgerId") Long ledgerId, @Param("fromDate") LocalDate fromDate);

  @Query("select vl from VoucherLine vl join fetch vl.voucher where vl.ledger.id = :ledgerId "
      + "and (:fromDate is null or vl.voucher.voucherDate >= :fromDate) "
      + "and (:toDate is null or vl.voucher.voucherDate <= :toDate) "
      + "order by vl.voucher.voucherDate, vl.id")
  List<VoucherLine> findStatementLines(@Param("ledgerId") Long ledgerId,
                                       @Param("fromDate") LocalDate fromDate,
                                       @Param("toDate") LocalDate toDate);
}
