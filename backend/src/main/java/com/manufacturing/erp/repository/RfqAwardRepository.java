package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.RfqAward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RfqAwardRepository extends JpaRepository<RfqAward, Long> {}
