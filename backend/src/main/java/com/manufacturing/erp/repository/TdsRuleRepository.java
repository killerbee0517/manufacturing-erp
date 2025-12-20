package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.TdsRule;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TdsRuleRepository extends JpaRepository<TdsRule, Long> {
  Optional<TdsRule> findFirstBySectionCodeAndEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqual(
      String sectionCode, LocalDate from, LocalDate to);
}
