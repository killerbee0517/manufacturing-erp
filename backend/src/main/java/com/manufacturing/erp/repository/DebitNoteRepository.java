package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.DebitNote;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DebitNoteRepository extends JpaRepository<DebitNote, Long> {
  Optional<DebitNote> findFirstByPurchaseInvoiceId(Long purchaseInvoiceId);

  List<DebitNote> findByPurchaseOrderCompanyId(Long companyId);

  Optional<DebitNote> findByIdAndPurchaseOrderCompanyId(Long id, Long companyId);
}
