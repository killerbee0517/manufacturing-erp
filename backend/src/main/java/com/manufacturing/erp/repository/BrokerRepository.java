package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.Broker;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrokerRepository extends JpaRepository<Broker, Long> {
  List<Broker> findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(String name, String code);
}
