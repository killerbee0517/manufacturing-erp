package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Vehicle;
import com.manufacturing.erp.dto.MasterDtos;
import com.manufacturing.erp.repository.VehicleRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {
  private final VehicleRepository vehicleRepository;

  public VehicleController(VehicleRepository vehicleRepository) {
    this.vehicleRepository = vehicleRepository;
  }

  @GetMapping
  public List<MasterDtos.VehicleResponse> list(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) Integer limit) {
    List<Vehicle> vehicles = (q == null || q.isBlank())
        ? vehicleRepository.findAll()
        : vehicleRepository.findByVehicleNoContainingIgnoreCase(q);
    return applyLimit(vehicles, limit).stream().map(this::toResponse).toList();
  }

  @GetMapping("/{id}")
  public MasterDtos.VehicleResponse get(@PathVariable Long id) {
    Vehicle vehicle = vehicleRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));
    return toResponse(vehicle);
  }

  @PostMapping
  public MasterDtos.VehicleResponse create(@Valid @RequestBody MasterDtos.VehicleRequest request) {
    Vehicle vehicle = new Vehicle();
    applyRequest(vehicle, request);
    Vehicle saved = vehicleRepository.save(vehicle);
    return toResponse(saved);
  }

  @PutMapping("/{id}")
  public MasterDtos.VehicleResponse update(@PathVariable Long id, @Valid @RequestBody MasterDtos.VehicleRequest request) {
    Vehicle vehicle = vehicleRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));
    applyRequest(vehicle, request);
    Vehicle saved = vehicleRepository.save(vehicle);
    return toResponse(saved);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    if (!vehicleRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found");
    }
    vehicleRepository.deleteById(id);
  }

  private void applyRequest(Vehicle vehicle, MasterDtos.VehicleRequest request) {
    vehicle.setVehicleNo(request.vehicleNo());
    vehicle.setVehicleType(request.vehicleType());
    vehicle.setRegistrationDate(request.registrationDate());
  }

  private MasterDtos.VehicleResponse toResponse(Vehicle vehicle) {
    return new MasterDtos.VehicleResponse(
        vehicle.getId(),
        vehicle.getVehicleNo(),
        vehicle.getVehicleType(),
        vehicle.getRegistrationDate());
  }

  private List<Vehicle> applyLimit(List<Vehicle> vehicles, Integer limit) {
    if (limit == null) {
      return vehicles;
    }
    return vehicles.stream().limit(limit).toList();
  }
}
