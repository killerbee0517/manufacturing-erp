package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.ProcessStep;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessStepRepository extends JpaRepository<ProcessStep, Long> {
}
