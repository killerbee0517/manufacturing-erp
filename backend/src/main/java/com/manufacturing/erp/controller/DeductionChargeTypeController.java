package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.DeductionChargeType;
import com.manufacturing.erp.domain.Enums.CalcType;
import com.manufacturing.erp.dto.DeductionChargeTypeDtos;
import com.manufacturing.erp.repository.DeductionChargeTypeRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deduction-charge-types")
public class DeductionChargeTypeController {
  private final DeductionChargeTypeRepository repository;

  public DeductionChargeTypeController(DeductionChargeTypeRepository repository) {
    this.repository = repository;
  }

  @GetMapping
  public List<DeductionChargeTypeDtos.DeductionChargeTypeResponse> list() {
    return repository.findAll().stream().map(this::toResponse).toList();
  }

  @GetMapping("/{id}")
  public DeductionChargeTypeDtos.DeductionChargeTypeResponse get(@PathVariable Long id) {
    DeductionChargeType entity = repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Charge/Deduction type not found"));
    return toResponse(entity);
  }

  @PostMapping
  public DeductionChargeTypeDtos.DeductionChargeTypeResponse create(
      @Valid @RequestBody DeductionChargeTypeDtos.DeductionChargeTypeRequest request) {
    if (repository.existsByCode(request.code())) {
      throw new IllegalArgumentException("Code already exists");
    }
    DeductionChargeType entity = new DeductionChargeType();
    apply(entity, request);
    return toResponse(repository.save(entity));
  }

  @PutMapping("/{id}")
  public DeductionChargeTypeDtos.DeductionChargeTypeResponse update(
      @PathVariable Long id,
      @Valid @RequestBody DeductionChargeTypeDtos.DeductionChargeTypeRequest request) {
    DeductionChargeType entity = repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Charge/Deduction type not found"));
    apply(entity, request);
    return toResponse(repository.save(entity));
  }

  private void apply(DeductionChargeType entity, DeductionChargeTypeDtos.DeductionChargeTypeRequest request) {
    entity.setCode(request.code());
    entity.setName(request.name());
    entity.setDefaultCalcType(CalcType.valueOf(request.defaultCalcType().toUpperCase()));
    entity.setDefaultRate(request.defaultRate());
    entity.setDeduction(Boolean.TRUE.equals(request.isDeduction()));
    entity.setEnabled(request.enabled() == null || request.enabled());
  }

  private DeductionChargeTypeDtos.DeductionChargeTypeResponse toResponse(DeductionChargeType entity) {
    return new DeductionChargeTypeDtos.DeductionChargeTypeResponse(
        entity.getId(),
        entity.getCode(),
        entity.getName(),
        entity.getDefaultCalcType() != null ? entity.getDefaultCalcType().name() : null,
        entity.getDefaultRate(),
        entity.isDeduction(),
        entity.isEnabled());
  }
}
