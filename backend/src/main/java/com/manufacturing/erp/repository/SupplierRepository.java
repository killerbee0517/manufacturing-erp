package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {}
