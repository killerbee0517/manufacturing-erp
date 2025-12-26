package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Enums.LedgerType;
import com.manufacturing.erp.domain.Ledger;
import com.manufacturing.erp.domain.Supplier;
import com.manufacturing.erp.dto.MasterDtos;
import com.manufacturing.erp.repository.BankRepository;
import com.manufacturing.erp.repository.SupplierRepository;
import com.manufacturing.erp.service.LedgerService;
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
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {
  private final SupplierRepository supplierRepository;
  private final BankRepository bankRepository;
  private final LedgerService ledgerService;

  public SupplierController(SupplierRepository supplierRepository, BankRepository bankRepository, LedgerService ledgerService) {
    this.supplierRepository = supplierRepository;
    this.bankRepository = bankRepository;
    this.ledgerService = ledgerService;
  }

  @GetMapping
  public List<MasterDtos.SupplierResponse> list(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) Integer limit) {
    List<Supplier> suppliers = (q == null || q.isBlank())
        ? supplierRepository.findAll()
        : supplierRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(q, q);
    return applyLimit(suppliers, limit).stream().map(this::toResponse).toList();
  }

  @GetMapping("/{id}")
  public MasterDtos.SupplierResponse get(@PathVariable Long id) {
    Supplier supplier = supplierRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found"));
    return toResponse(supplier);
  }

  @GetMapping("/{id}/balance")
  public com.manufacturing.erp.dto.LedgerDtos.LedgerBalanceResponse getBalance(@PathVariable Long id) {
    Supplier supplier = supplierRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found"));
    Ledger ledger = supplier.getLedger();
    if (ledger == null) {
      ledger = ledgerService.createLedger(supplier.getName(), LedgerType.SUPPLIER, "SUPPLIER", supplier.getId());
      supplier.setLedger(ledger);
      supplierRepository.save(supplier);
    }
    return new com.manufacturing.erp.dto.LedgerDtos.LedgerBalanceResponse(
        ledger.getId(),
        ledgerService.getBalance(ledger.getId()));
  }

  @PostMapping
  @Transactional
  public MasterDtos.SupplierResponse create(@Valid @RequestBody MasterDtos.SupplierRequest request) {
    Supplier supplier = new Supplier();
    applyRequest(supplier, request);
    Supplier saved = supplierRepository.save(supplier);
    Ledger ledger = ledgerService.createLedger(request.name(), LedgerType.SUPPLIER, "SUPPLIER", saved.getId());
    saved.setLedger(ledger);
    Supplier updated = supplierRepository.save(saved);
    return toResponse(updated);
  }

  @PutMapping("/{id}")
  @Transactional
  public MasterDtos.SupplierResponse update(@PathVariable Long id, @Valid @RequestBody MasterDtos.SupplierRequest request) {
    Supplier supplier = supplierRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found"));
    applyRequest(supplier, request);
    Supplier saved = supplierRepository.save(supplier);
    if (saved.getLedger() == null) {
      Ledger ledger = ledgerService.createLedger(saved.getName(), LedgerType.SUPPLIER, "SUPPLIER", saved.getId());
      saved.setLedger(ledger);
      saved = supplierRepository.save(saved);
    }
    return toResponse(saved);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    if (!supplierRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found");
    }
    supplierRepository.deleteById(id);
  }

  private void applyRequest(Supplier supplier, MasterDtos.SupplierRequest request) {
    supplier.setName(request.name());
    supplier.setCode(request.code());
    supplier.setPan(request.pan());
    supplier.setAddress(request.address());
    supplier.setState(request.state());
    supplier.setCountry(request.country());
    supplier.setPinCode(request.pinCode());
    supplier.setGstNo(request.gstNo());
    supplier.setContact(request.contact());
    supplier.setEmail(request.email());
    supplier.setSupplierType(request.supplierType());
    supplier.setCreditPeriod(request.creditPeriod());
    if (request.bankId() != null) {
      supplier.setBank(bankRepository.findById(request.bankId())
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bank not found")));
    } else {
      supplier.setBank(null);
    }
  }

  private MasterDtos.SupplierResponse toResponse(Supplier supplier) {
    return new MasterDtos.SupplierResponse(
        supplier.getId(),
        supplier.getName(),
        supplier.getCode(),
        supplier.getPan(),
        supplier.getAddress(),
        supplier.getState(),
        supplier.getCountry(),
        supplier.getPinCode(),
        supplier.getGstNo(),
        supplier.getContact(),
        supplier.getEmail(),
        supplier.getBank() != null ? supplier.getBank().getId() : null,
        supplier.getBank() != null ? supplier.getBank().getName() : null,
        supplier.getSupplierType(),
        supplier.getCreditPeriod(),
        supplier.getLedger() != null ? supplier.getLedger().getId() : null,
        supplier.getLedger() != null ? ledgerService.getBalance(supplier.getLedger().getId()) : null);
  }

  private List<Supplier> applyLimit(List<Supplier> suppliers, Integer limit) {
    if (limit == null) {
      return suppliers;
    }
    return suppliers.stream().limit(limit).toList();
  }
}
