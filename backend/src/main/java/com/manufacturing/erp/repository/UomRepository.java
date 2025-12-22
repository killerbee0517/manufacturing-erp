package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.Uom;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UomRepository extends JpaRepository<Uom, Long> {
  Optional<Uom> findByCode(String code);

  List<Uom> findByCodeContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String code, String description);
}
