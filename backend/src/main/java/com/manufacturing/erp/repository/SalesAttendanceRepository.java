package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.SalesAttendance;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SalesAttendanceRepository extends JpaRepository<SalesAttendance, Long> {
  @Query("""
      select sa from SalesAttendance sa
      where sa.company.id = :companyId
        and (:fromDate is null or sa.attendanceDate >= :fromDate)
        and (:toDate is null or sa.attendanceDate <= :toDate)
      order by sa.attendanceDate desc, sa.id desc
      """)
  List<SalesAttendance> search(@Param("companyId") Long companyId,
                               @Param("fromDate") LocalDate fromDate,
                               @Param("toDate") LocalDate toDate);

  Optional<SalesAttendance> findByIdAndCompanyId(Long id, Long companyId);
}
