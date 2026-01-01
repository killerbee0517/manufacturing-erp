package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.Enums.PaymentStatus;
import com.manufacturing.erp.domain.PaymentVoucher;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentVoucherRepository extends JpaRepository<PaymentVoucher, Long> {
  @Query("""
      select pv from PaymentVoucher pv
      where pv.company.id = :companyId
        and (:fromDate is null or pv.voucherDate >= :fromDate)
        and (:toDate is null or pv.voucherDate <= :toDate)
      order by pv.voucherDate desc, pv.id desc
      """)
  List<PaymentVoucher> findForExport(@Param("companyId") Long companyId,
                                     @Param("fromDate") LocalDate fromDate,
                                     @Param("toDate") LocalDate toDate);

  Page<PaymentVoucher> findByCompanyId(Long companyId, Pageable pageable);

  List<PaymentVoucher> findByCompanyIdAndStatusIn(Long companyId, List<PaymentStatus> statuses);

  java.util.Optional<PaymentVoucher> findByIdAndCompanyId(Long id, Long companyId);
}
