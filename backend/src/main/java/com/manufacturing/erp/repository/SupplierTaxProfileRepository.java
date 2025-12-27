package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.SupplierTaxProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierTaxProfileRepository extends JpaRepository<SupplierTaxProfile, Long> {
  Optional<SupplierTaxProfile> findBySupplierId(Long supplierId);
  void deleteBySupplierId(Long supplierId);
}
