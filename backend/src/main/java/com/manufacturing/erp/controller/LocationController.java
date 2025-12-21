package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Enums.LocationType;
import com.manufacturing.erp.domain.Location;
import com.manufacturing.erp.dto.MasterDtos;
import com.manufacturing.erp.repository.LocationRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locations")
public class LocationController {
  private final LocationRepository locationRepository;

  public LocationController(LocationRepository locationRepository) {
    this.locationRepository = locationRepository;
  }

  @GetMapping
  public List<MasterDtos.LocationResponse> list() {
    return locationRepository.findAll().stream()
        .map(location -> new MasterDtos.LocationResponse(
            location.getId(),
            location.getName(),
            location.getCode(),
            location.getLocationType().name()))
        .toList();
  }

  @PostMapping
  public MasterDtos.LocationResponse create(@Valid @RequestBody MasterDtos.LocationRequest request) {
    Location location = new Location();
    location.setName(request.name());
    location.setCode(request.code());
    location.setLocationType(LocationType.valueOf(request.locationType().toUpperCase()));
    Location saved = locationRepository.save(location);
    return new MasterDtos.LocationResponse(saved.getId(), saved.getName(), saved.getCode(), saved.getLocationType().name());
  }
}
