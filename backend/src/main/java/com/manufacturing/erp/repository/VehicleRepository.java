package com.manufacturing.erp.repository;

import com.manufacturing.erp.domain.Vehicle;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
  List<Vehicle> findByVehicleNoContainingIgnoreCase(String vehicleNo);
}
