package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.Delivery;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
  List<Delivery> findByCompanyId(Long companyId);

  Optional<Delivery> findByIdAndCompanyId(Long id, Long companyId);
}
