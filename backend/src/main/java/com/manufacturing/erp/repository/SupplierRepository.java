package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.Supplier;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
  List<Supplier> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(String name, String code);
  Optional<Supplier> findByPartyId(Long partyId);
}
