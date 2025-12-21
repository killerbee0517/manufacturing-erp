package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Vehicle;
import com.manufacturing.erp.dto.MasterDtos;
import com.manufacturing.erp.repository.VehicleRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
  private final VehicleRepository vehicleRepository;

  public VehicleController(VehicleRepository vehicleRepository) {
    this.vehicleRepository = vehicleRepository;
  }

  @GetMapping
  public List<MasterDtos.VehicleResponse> list() {
    return vehicleRepository.findAll().stream()
        .map(vehicle -> new MasterDtos.VehicleResponse(vehicle.getId(), vehicle.getVehicleNo()))
        .toList();
  }

  @PostMapping
  public MasterDtos.VehicleResponse create(@Valid @RequestBody MasterDtos.VehicleRequest request) {
    Vehicle vehicle = new Vehicle();
    vehicle.setVehicleNo(request.vehicleNo());
    Vehicle saved = vehicleRepository.save(vehicle);
    return new MasterDtos.VehicleResponse(saved.getId(), saved.getVehicleNo());
  }
}
