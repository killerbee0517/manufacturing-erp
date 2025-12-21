package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Supplier;
import com.manufacturing.erp.dto.MasterDtos;
import com.manufacturing.erp.repository.SupplierRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {
  private final SupplierRepository supplierRepository;

  public SupplierController(SupplierRepository supplierRepository) {
    this.supplierRepository = supplierRepository;
  }

  @GetMapping
  public List<MasterDtos.SupplierResponse> list() {
    return supplierRepository.findAll().stream()
        .map(supplier -> new MasterDtos.SupplierResponse(
            supplier.getId(), supplier.getName(), supplier.getCode(), supplier.getPan()))
        .toList();
  }

  @PostMapping
  public MasterDtos.SupplierResponse create(@Valid @RequestBody MasterDtos.SupplierRequest request) {
    Supplier supplier = new Supplier();
    supplier.setName(request.name());
    supplier.setCode(request.code());
    supplier.setPan(request.pan());
    Supplier saved = supplierRepository.save(supplier);
    return new MasterDtos.SupplierResponse(saved.getId(), saved.getName(), saved.getCode(), saved.getPan());
  }
}
