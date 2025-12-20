package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.BrokerCommissionRule;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrokerCommissionRuleRepository extends JpaRepository<BrokerCommissionRule, Long> {
  Optional<BrokerCommissionRule> findFirstByBrokerId(Long brokerId);
}
