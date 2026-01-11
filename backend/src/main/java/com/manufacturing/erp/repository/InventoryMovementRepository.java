package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.InventoryMovement;
import com.manufacturing.erp.domain.Enums.InventoryLocationType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
  List<InventoryMovement> findByLocationTypeAndLocationIdAndCompanyId(InventoryLocationType locationType,
                                                                      Long locationId,
                                                                      Long companyId);
}
