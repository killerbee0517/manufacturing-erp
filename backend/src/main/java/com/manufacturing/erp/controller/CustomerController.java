package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Company;
import com.manufacturing.erp.domain.Customer;
import com.manufacturing.erp.domain.Enums.LedgerType;
import com.manufacturing.erp.domain.Enums.PartyRoleType;
import com.manufacturing.erp.domain.Enums.PartyStatus;
import com.manufacturing.erp.domain.Ledger;
import com.manufacturing.erp.domain.Party;
import com.manufacturing.erp.domain.PartyRole;
import com.manufacturing.erp.dto.MasterDtos;
import com.manufacturing.erp.repository.BankRepository;
import com.manufacturing.erp.repository.CompanyRepository;
import com.manufacturing.erp.repository.CustomerRepository;
import com.manufacturing.erp.repository.PartyRepository;
import com.manufacturing.erp.repository.PartyRoleRepository;
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
@RequestMapping("/api/customers")
public class CustomerController {
  private final CustomerRepository customerRepository;
  private final BankRepository bankRepository;
  private final PartyRepository partyRepository;
  private final PartyRoleRepository partyRoleRepository;
  private final CompanyRepository companyRepository;
  private final CompanyContext companyContext;
  private final LedgerAccountService ledgerAccountService;
  private final LedgerService ledgerService;

  public CustomerController(CustomerRepository customerRepository, BankRepository bankRepository,
                            PartyRepository partyRepository, PartyRoleRepository partyRoleRepository,
                            CompanyRepository companyRepository, CompanyContext companyContext,
                            LedgerAccountService ledgerAccountService, LedgerService ledgerService) {
    this.customerRepository = customerRepository;
    this.bankRepository = bankRepository;
    this.partyRepository = partyRepository;
    this.partyRoleRepository = partyRoleRepository;
    this.companyRepository = companyRepository;
    this.companyContext = companyContext;
    this.ledgerAccountService = ledgerAccountService;
    this.ledgerService = ledgerService;
  }

  @GetMapping
  public List<MasterDtos.CustomerResponse> list(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) Integer limit) {
    List<Customer> customers = (q == null || q.isBlank())
        ? customerRepository.findAll()
        : customerRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(q, q);
    return applyLimit(customers, limit).stream().map(this::toResponse).toList();
  }

  @GetMapping("/{id}")
  public MasterDtos.CustomerResponse get(@PathVariable Long id) {
    Customer customer = customerRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    return toResponse(customer);
  }

  @GetMapping("/{id}/balance")
  public com.manufacturing.erp.dto.LedgerDtos.LedgerBalanceResponse getBalance(@PathVariable Long id) {
    Customer customer = customerRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    Ledger ledger = customer.getLedger();
    if (ledger == null) {
      ledger = ledgerService.createLedger(customer.getName(), LedgerType.CUSTOMER, "CUSTOMER", customer.getId());
      customer.setLedger(ledger);
      customerRepository.save(customer);
    }
    return new com.manufacturing.erp.dto.LedgerDtos.LedgerBalanceResponse(
        ledger.getId(),
        ledgerService.getBalance(ledger.getId()));
  }

  @PostMapping
  @Transactional
  public MasterDtos.CustomerResponse create(@Valid @RequestBody MasterDtos.CustomerRequest request) {
    Customer customer = new Customer();
    Party party = resolveParty(request, customer);
    applyRequest(customer, request, party);
    Customer saved = customerRepository.save(customer);
    Ledger ledger = ledgerService.createLedger(request.name(), LedgerType.CUSTOMER, "CUSTOMER", saved.getId());
    saved.setLedger(ledger);
    Customer updated = customerRepository.save(saved);
    return toResponse(updated);
  }

  @PutMapping("/{id}")
  @Transactional
  public MasterDtos.CustomerResponse update(@PathVariable Long id, @Valid @RequestBody MasterDtos.CustomerRequest request) {
    Customer customer = customerRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    Party party = resolveParty(request, customer);
    applyRequest(customer, request, party);
    Customer saved = customerRepository.save(customer);
    if (saved.getLedger() == null) {
      Ledger ledger = ledgerService.createLedger(saved.getName(), LedgerType.CUSTOMER, "CUSTOMER", saved.getId());
      saved.setLedger(ledger);
      saved = customerRepository.save(saved);
    }
    return toResponse(saved);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    if (!customerRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
    }
    customerRepository.deleteById(id);
  }

  private void applyRequest(Customer customer, MasterDtos.CustomerRequest request, Party party) {
    customer.setName(request.name());
    customer.setCode(request.code());
    customer.setAddress(request.address());
    customer.setState(request.state());
    customer.setCountry(request.country());
    customer.setPinCode(request.pinCode());
    customer.setPan(request.pan());
    customer.setGstNo(request.gstNo());
    customer.setContact(request.contact());
    customer.setEmail(request.email());
    customer.setCreditPeriod(request.creditPeriod());
    customer.setParty(party);
    if (request.bankId() != null) {
      customer.setBank(bankRepository.findById(request.bankId())
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bank not found")));
    } else {
      customer.setBank(null);
    }
  }

  private MasterDtos.CustomerResponse toResponse(Customer customer) {
    return new MasterDtos.CustomerResponse(
        customer.getId(),
        customer.getParty() != null ? customer.getParty().getId() : null,
        customer.getName(),
        customer.getCode(),
        customer.getAddress(),
        customer.getState(),
        customer.getCountry(),
        customer.getPinCode(),
        customer.getPan(),
        customer.getGstNo(),
        customer.getContact(),
        customer.getEmail(),
        customer.getBank() != null ? customer.getBank().getId() : null,
        customer.getBank() != null ? customer.getBank().getName() : null,
        customer.getCreditPeriod(),
        customer.getLedger() != null ? customer.getLedger().getId() : null,
        customer.getLedger() != null ? ledgerService.getBalance(customer.getLedger().getId()) : null);
  }

  private Party resolveParty(MasterDtos.CustomerRequest request, Customer customer) {
    Company company = requireCompany();
    Party party = null;
    if (request.partyId() != null) {
      party = partyRepository.findById(request.partyId())
          .filter(p -> p.getCompany().getId().equals(company.getId()))
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Party not found"));
    } else if (customer.getParty() != null) {
      party = customer.getParty();
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

  private void syncRole(Party party, Company company, MasterDtos.CustomerRequest request) {
    PartyRole role = partyRoleRepository.findByCompanyIdAndPartyId(company.getId(), party.getId()).stream()
        .filter(r -> r.getRoleType() == PartyRoleType.CUSTOMER)
        .findFirst()
        .orElseGet(() -> {
          PartyRole fresh = new PartyRole();
          fresh.setCompany(company);
          fresh.setParty(party);
          fresh.setRoleType(PartyRoleType.CUSTOMER);
          return fresh;
        });
    role.setCreditPeriodDays(request.creditPeriod());
    role.setActive(true);
    partyRoleRepository.save(role);
    ledgerAccountService.ensurePartyLedger(company, party, com.manufacturing.erp.domain.Enums.LedgerType.CUSTOMER, party.getName());
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

  private List<Customer> applyLimit(List<Customer> customers, Integer limit) {
    if (limit == null) {
      return customers;
    }
    return customers.stream().limit(limit).toList();
  }
}
