package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.Location;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
  Optional<Location> findByCode(String code);
}
