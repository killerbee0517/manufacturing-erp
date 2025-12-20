package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {}
