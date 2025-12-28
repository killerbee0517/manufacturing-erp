package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.DebitNoteLine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DebitNoteLineRepository extends JpaRepository<DebitNoteLine, Long> {
  List<DebitNoteLine> findByDebitNoteId(Long debitNoteId);
}
