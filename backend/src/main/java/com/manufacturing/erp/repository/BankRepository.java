package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.Bank;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankRepository extends JpaRepository<Bank, Long> {
  List<Bank> findByNameContainingIgnoreCase(String name);
}
