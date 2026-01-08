package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Company;
import com.manufacturing.erp.domain.Enums.LedgerType;
import com.manufacturing.erp.domain.Enums.PartyRoleType;
import com.manufacturing.erp.domain.Enums.PartyStatus;
import com.manufacturing.erp.domain.Ledger;
import com.manufacturing.erp.domain.Party;
import com.manufacturing.erp.domain.PartyRole;
import com.manufacturing.erp.domain.Supplier;
import com.manufacturing.erp.dto.MasterDtos;
import com.manufacturing.erp.repository.BankRepository;
import com.manufacturing.erp.repository.CompanyRepository;
import com.manufacturing.erp.repository.PartyRepository;
import com.manufacturing.erp.repository.PartyRoleRepository;
import com.manufacturing.erp.repository.SupplierRepository;
import com.manufacturing.erp.repository.SupplierTaxProfileRepository;
import com.manufacturing.erp.security.CompanyContext;
import com.manufacturing.erp.service.LedgerAccountService;
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
  private final PartyRepository partyRepository;
  private final PartyRoleRepository partyRoleRepository;
  private final CompanyRepository companyRepository;
  private final CompanyContext companyContext;
  private final LedgerAccountService ledgerAccountService;
  private final LedgerService ledgerService;
  private final SupplierTaxProfileRepository supplierTaxProfileRepository;

  public SupplierController(SupplierRepository supplierRepository, BankRepository bankRepository,
                            PartyRepository partyRepository, PartyRoleRepository partyRoleRepository,
                            CompanyRepository companyRepository, CompanyContext companyContext,
                            LedgerAccountService ledgerAccountService, LedgerService ledgerService,
                            SupplierTaxProfileRepository supplierTaxProfileRepository) {
    this.supplierRepository = supplierRepository;
    this.bankRepository = bankRepository;
    this.partyRepository = partyRepository;
    this.partyRoleRepository = partyRoleRepository;
    this.companyRepository = companyRepository;
    this.companyContext = companyContext;
    this.ledgerAccountService = ledgerAccountService;
    this.ledgerService = ledgerService;
    this.supplierTaxProfileRepository = supplierTaxProfileRepository;
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
    Party party = resolveParty(request, supplier);
    applyRequest(supplier, request, party);
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
    Party party = resolveParty(request, supplier);
    applyRequest(supplier, request, party);
    Supplier saved = supplierRepository.save(supplier);
    if (saved.getLedger() == null) {
      Ledger ledger = ledgerService.createLedger(saved.getName(), LedgerType.SUPPLIER, "SUPPLIER", saved.getId());
      saved.setLedger(ledger);
      saved = supplierRepository.save(saved);
    }
    return toResponse(saved);
  }

  @DeleteMapping("/{id}")
  @Transactional
  public void delete(@PathVariable Long id) {
    if (!supplierRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Supplier not found");
    }
    supplierTaxProfileRepository.deleteBySupplierId(id);
    supplierRepository.deleteById(id);
  }

  private void applyRequest(Supplier supplier, MasterDtos.SupplierRequest request, Party party) {
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
    supplier.setCreditPeriod(request.creditPeriod());
    supplier.setParty(party);
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
        supplier.getParty() != null ? supplier.getParty().getId() : null,
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
        supplier.getCreditPeriod(),
        supplier.getLedger() != null ? supplier.getLedger().getId() : null,
        supplier.getLedger() != null ? ledgerService.getBalance(supplier.getLedger().getId()) : null);
  }

  private Party resolveParty(MasterDtos.SupplierRequest request, Supplier supplier) {
    Company company = requireCompany();
    Party party = null;
    if (request.partyId() != null) {
      party = partyRepository.findById(request.partyId())
          .filter(p -> p.getCompany().getId().equals(company.getId()))
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Party not found"));
    } else if (supplier.getParty() != null) {
      party = supplier.getParty();
    }
    if (party == null) {
      party = new Party();
      party.setCompany(company);
      party.setPartyCode(resolvePartyCode(company));
      party.setStatus(PartyStatus.ACTIVE);
    }
    party.setName(request.name());
    party.setAddress(request.address());
    party.setState(request.state());
    party.setCountry(request.country());
    party.setPinCode(request.pinCode());
    party.setPan(request.pan());
    party.setGstNo(request.gstNo());
    party.setContact(request.contact());
    party.setEmail(request.email());
    Party saved = partyRepository.save(party);
    syncRole(saved, company, request);
    return saved;
  }

  private void syncRole(Party party, Company company, MasterDtos.SupplierRequest request) {
    PartyRole role = partyRoleRepository.findByCompanyIdAndPartyId(company.getId(), party.getId()).stream()
        .filter(r -> r.getRoleType() == PartyRoleType.SUPPLIER)
        .findFirst()
        .orElseGet(() -> {
          PartyRole fresh = new PartyRole();
          fresh.setCompany(company);
          fresh.setParty(party);
          fresh.setRoleType(PartyRoleType.SUPPLIER);
          return fresh;
        });
    role.setCreditPeriodDays(request.creditPeriod());
    role.setActive(true);
    partyRoleRepository.save(role);
    ledgerAccountService.ensurePartyLedger(company, party, com.manufacturing.erp.domain.Enums.LedgerType.SUPPLIER, party.getName());
  }

  private Company requireCompany() {
    Long companyId = companyContext.getCompanyId();
    if (companyId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing company context");
    }
    return companyRepository.findById(companyId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
  }

  private String resolvePartyCode(Company company) {
    return "PTY-" + (partyRepository.countByCompanyId(company.getId()) + 1);
  }

  private List<Supplier> applyLimit(List<Supplier> suppliers, Integer limit) {
    if (limit == null) {
      return suppliers;
    }
    return suppliers.stream().limit(limit).toList();
  }
}
