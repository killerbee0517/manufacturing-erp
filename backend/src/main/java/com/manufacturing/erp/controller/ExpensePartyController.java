package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Company;
import com.manufacturing.erp.domain.Enums.PartyRoleType;
import com.manufacturing.erp.domain.Enums.PartyStatus;
import com.manufacturing.erp.domain.Enums.PayablePartyType;
import com.manufacturing.erp.domain.ExpenseParty;
import com.manufacturing.erp.domain.Party;
import com.manufacturing.erp.domain.PartyRole;
import com.manufacturing.erp.dto.ExpensePartyDtos;
import com.manufacturing.erp.repository.CompanyRepository;
import com.manufacturing.erp.repository.ExpensePartyRepository;
import com.manufacturing.erp.repository.PartyRepository;
import com.manufacturing.erp.repository.PartyRoleRepository;
import com.manufacturing.erp.security.CompanyContext;
import com.manufacturing.erp.service.LedgerAccountService;
import com.manufacturing.erp.service.LedgerService;
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
@RequestMapping("/api/expense-parties")
public class ExpensePartyController {
  private final ExpensePartyRepository repository;
  private final PartyRepository partyRepository;
  private final PartyRoleRepository partyRoleRepository;
  private final CompanyRepository companyRepository;
  private final CompanyContext companyContext;
  private final LedgerAccountService ledgerAccountService;
  private final LedgerService ledgerService;

  public ExpensePartyController(ExpensePartyRepository repository, PartyRepository partyRepository,
                                PartyRoleRepository partyRoleRepository, CompanyRepository companyRepository,
                                CompanyContext companyContext, LedgerAccountService ledgerAccountService,
                                LedgerService ledgerService) {
    this.repository = repository;
    this.partyRepository = partyRepository;
    this.partyRoleRepository = partyRoleRepository;
    this.companyRepository = companyRepository;
    this.companyContext = companyContext;
    this.ledgerAccountService = ledgerAccountService;
    this.ledgerService = ledgerService;
  }

  @GetMapping
  public List<ExpensePartyDtos.ExpensePartyResponse> list() {
    return repository.findAll().stream().map(this::toResponse).toList();
  }

  @GetMapping("/{id}")
  public ExpensePartyDtos.ExpensePartyResponse get(@PathVariable Long id) {
    ExpenseParty entity = repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Expense party not found"));
    return toResponse(entity);
  }

  @PostMapping
  public ExpensePartyDtos.ExpensePartyResponse create(@Valid @RequestBody ExpensePartyDtos.ExpensePartyRequest request) {
    ExpenseParty entity = new ExpenseParty();
    Party party = resolveParty(request, entity);
    apply(entity, request, party);
    ensureLedger(entity);
    return toResponse(repository.save(entity));
  }

  @PutMapping("/{id}")
  public ExpensePartyDtos.ExpensePartyResponse update(@PathVariable Long id,
                                                      @Valid @RequestBody ExpensePartyDtos.ExpensePartyRequest request) {
    ExpenseParty entity = repository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Expense party not found"));
    Party party = resolveParty(request, entity);
    apply(entity, request, party);
    ensureLedger(entity);
    return toResponse(repository.save(entity));
  }

  private void apply(ExpenseParty entity, ExpensePartyDtos.ExpensePartyRequest request, Party party) {
    entity.setName(request.name());
    entity.setPartyType(PayablePartyType.valueOf(request.partyType().toUpperCase()));
    entity.setParty(party);
  }

  private void ensureLedger(ExpenseParty entity) {
    if (entity.getLedger() != null) {
      return;
    }
    var ledger = ledgerService.findOrCreateLedger(entity.getName(), com.manufacturing.erp.domain.Enums.LedgerType.GENERAL);
    entity.setLedger(ledger);
  }

  private ExpensePartyDtos.ExpensePartyResponse toResponse(ExpenseParty entity) {
    return new ExpensePartyDtos.ExpensePartyResponse(
        entity.getId(),
        entity.getParty() != null ? entity.getParty().getId() : null,
        entity.getName(),
        entity.getPartyType() != null ? entity.getPartyType().name() : null,
        entity.getLedger() != null ? entity.getLedger().getId() : null);
  }

  private Party resolveParty(ExpensePartyDtos.ExpensePartyRequest request, ExpenseParty entity) {
    Company company = requireCompany();
    Party party = null;
    if (request.partyId() != null) {
      party = partyRepository.findById(request.partyId())
          .filter(p -> p.getCompany().getId().equals(company.getId()))
          .orElseThrow(() -> new IllegalArgumentException("Party not found"));
    } else if (entity.getParty() != null) {
      party = entity.getParty();
    }
    if (party == null) {
      party = new Party();
      party.setCompany(company);
      party.setPartyCode(resolvePartyCode(company));
      party.setStatus(PartyStatus.ACTIVE);
    }
    party.setName(request.name());
    Party saved = partyRepository.save(party);
    syncRole(saved, company);
    return saved;
  }

  private void syncRole(Party party, Company company) {
    PartyRole role = partyRoleRepository.findByCompanyIdAndPartyId(company.getId(), party.getId()).stream()
        .filter(r -> r.getRoleType() == PartyRoleType.EXPENSE)
        .findFirst()
        .orElseGet(() -> {
          PartyRole fresh = new PartyRole();
          fresh.setCompany(company);
          fresh.setParty(party);
          fresh.setRoleType(PartyRoleType.EXPENSE);
          return fresh;
        });
    role.setActive(true);
    partyRoleRepository.save(role);
    ledgerAccountService.ensurePartyLedger(company, party, com.manufacturing.erp.domain.Enums.LedgerType.EXPENSE, party.getName());
  }

  private Company requireCompany() {
    Long companyId = companyContext.getCompanyId();
    if (companyId == null) {
      throw new IllegalArgumentException("Missing company context");
    }
    return companyRepository.findById(companyId)
        .orElseThrow(() -> new IllegalArgumentException("Company not found"));
  }

  private String resolvePartyCode(Company company) {
    return "PTY-" + (partyRepository.countByCompanyId(company.getId()) + 1);
  }
}
