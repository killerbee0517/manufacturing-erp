package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.Customer;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
  List<Customer> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(String name, String code);
  Optional<Customer> findByPartyId(Long partyId);
}
