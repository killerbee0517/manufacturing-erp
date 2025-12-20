package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.WeighbridgeReading;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeighbridgeReadingRepository extends JpaRepository<WeighbridgeReading, Long> {
  List<WeighbridgeReading> findByTicketId(Long ticketId);
}
