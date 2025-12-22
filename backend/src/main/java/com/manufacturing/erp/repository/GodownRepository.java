package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.Godown;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GodownRepository extends JpaRepository<Godown, Long> {
  List<Godown> findByNameContainingIgnoreCase(String name);
}
