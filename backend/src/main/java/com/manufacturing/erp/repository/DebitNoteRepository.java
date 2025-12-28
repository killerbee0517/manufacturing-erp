package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.DebitNote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DebitNoteRepository extends JpaRepository<DebitNote, Long> {
  Optional<DebitNote> findFirstByPurchaseInvoiceId(Long purchaseInvoiceId);
}
