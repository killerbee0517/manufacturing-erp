package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Broker;
import com.manufacturing.erp.domain.BrokerCommissionRule;
import com.manufacturing.erp.domain.Company;
import com.manufacturing.erp.domain.Enums.BrokerCommissionType;
import com.manufacturing.erp.domain.Enums.BrokeragePaidBy;
import com.manufacturing.erp.domain.Enums.PartyRoleType;
import com.manufacturing.erp.domain.Enums.PartyStatus;
import com.manufacturing.erp.domain.Party;
import com.manufacturing.erp.domain.PartyRole;
import com.manufacturing.erp.dto.MasterDtos;
import com.manufacturing.erp.repository.BrokerRepository;
import com.manufacturing.erp.repository.BrokerCommissionRuleRepository;
import com.manufacturing.erp.repository.CompanyRepository;
import com.manufacturing.erp.repository.PartyRepository;
import com.manufacturing.erp.repository.PartyRoleRepository;
import com.manufacturing.erp.security.CompanyContext;
import com.manufacturing.erp.service.LedgerAccountService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/brokers")
public class BrokerController {
  private final BrokerRepository brokerRepository;
  private final BrokerCommissionRuleRepository brokerCommissionRuleRepository;
  private final PartyRepository partyRepository;
  private final PartyRoleRepository partyRoleRepository;
  private final CompanyRepository companyRepository;
  private final CompanyContext companyContext;
  private final LedgerAccountService ledgerAccountService;

  public BrokerController(BrokerRepository brokerRepository, BrokerCommissionRuleRepository brokerCommissionRuleRepository,
                          PartyRepository partyRepository, PartyRoleRepository partyRoleRepository,
                          CompanyRepository companyRepository, CompanyContext companyContext,
                          LedgerAccountService ledgerAccountService) {
    this.brokerRepository = brokerRepository;
    this.brokerCommissionRuleRepository = brokerCommissionRuleRepository;
    this.partyRepository = partyRepository;
    this.partyRoleRepository = partyRoleRepository;
    this.companyRepository = companyRepository;
    this.companyContext = companyContext;
    this.ledgerAccountService = ledgerAccountService;
  }

  @GetMapping
  public List<MasterDtos.BrokerResponse> list(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) Integer limit) {
    List<Broker> brokers = (q == null || q.isBlank())
        ? brokerRepository.findAll()
        : brokerRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(q, q);
    return applyLimit(brokers, limit).stream()
        .map(this::toResponse)
        .toList();
  }

  @GetMapping("/{id}")
  public MasterDtos.BrokerResponse get(@PathVariable Long id) {
    Broker broker = brokerRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Broker not found"));
    return toResponse(broker);
  }

  @PostMapping
  @Transactional
  public MasterDtos.BrokerResponse create(@Valid @RequestBody MasterDtos.BrokerRequest request) {
    Broker broker = new Broker();
    Party party = resolveParty(request, broker);
    broker.setName(request.name());
    broker.setCode(request.code());
    broker.setParty(party);
    Broker saved = brokerRepository.save(broker);
    syncCommissionRule(saved, request);
    return toResponse(saved);
  }

  @PutMapping("/{id}")
  @Transactional
  public MasterDtos.BrokerResponse update(@PathVariable Long id, @Valid @RequestBody MasterDtos.BrokerRequest request) {
    Broker broker = brokerRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Broker not found"));
    Party party = resolveParty(request, broker);
    broker.setName(request.name());
    broker.setCode(request.code());
    broker.setParty(party);
    Broker saved = brokerRepository.save(broker);
    syncCommissionRule(saved, request);
    return toResponse(saved);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    if (!brokerRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Broker not found");
    }
    brokerRepository.deleteById(id);
  }

  private List<Broker> applyLimit(List<Broker> brokers, Integer limit) {
    if (limit == null) {
      return brokers;
    }
    return brokers.stream().limit(limit).toList();
  }

  private MasterDtos.BrokerResponse toResponse(Broker broker) {
    PartyRole role = resolveRole(broker);
    return new MasterDtos.BrokerResponse(
        broker.getId(),
        broker.getParty() != null ? broker.getParty().getId() : null,
        broker.getName(),
        broker.getCode(),
        role != null && role.getBrokerCommissionType() != null ? role.getBrokerCommissionType().name() : null,
        role != null ? role.getBrokerCommissionRate() : null,
        role != null && role.getBrokeragePaidBy() != null ? role.getBrokeragePaidBy().name() : null);
  }

  private Party resolveParty(MasterDtos.BrokerRequest request, Broker broker) {
    Company company = requireCompany();
    Party party = null;
    if (request.partyId() != null) {
      party = partyRepository.findById(request.partyId())
          .filter(p -> p.getCompany().getId().equals(company.getId()))
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Party not found"));
    } else if (broker.getParty() != null) {
      party = broker.getParty();
    }
    if (party == null) {
      party = new Party();
      party.setCompany(company);
      party.setPartyCode(resolvePartyCode(company));
      party.setStatus(PartyStatus.ACTIVE);
    }
    party.setName(request.name());
    Party saved = partyRepository.save(party);
    syncRole(saved, company, request);
    return saved;
  }

  private void syncRole(Party party, Company company, MasterDtos.BrokerRequest request) {
    PartyRole role = resolveOrCreateRole(company, party);
    role.setBrokerCommissionType(parseBrokerCommissionType(request.brokerCommissionType()));
    role.setBrokerCommissionRate(request.brokerCommissionRate());
    role.setBrokeragePaidBy(parseBrokeragePaidBy(request.brokeragePaidBy()));
    role.setActive(true);
    partyRoleRepository.save(role);
    ledgerAccountService.ensurePartyLedger(company, party, com.manufacturing.erp.domain.Enums.LedgerType.BROKER, party.getName());
  }

  private PartyRole resolveRole(Broker broker) {
    if (broker.getParty() == null) {
      return null;
    }
    Company company = requireCompany();
    return partyRoleRepository.findByCompanyIdAndPartyId(company.getId(), broker.getParty().getId()).stream()
        .filter(r -> r.getRoleType() == PartyRoleType.BROKER)
        .findFirst()
        .orElse(null);
  }

  private PartyRole resolveOrCreateRole(Company company, Party party) {
    return partyRoleRepository.findByCompanyIdAndPartyId(company.getId(), party.getId()).stream()
        .filter(r -> r.getRoleType() == PartyRoleType.BROKER)
        .findFirst()
        .orElseGet(() -> {
          PartyRole fresh = new PartyRole();
          fresh.setCompany(company);
          fresh.setParty(party);
          fresh.setRoleType(PartyRoleType.BROKER);
          return fresh;
        });
  }

  private void syncCommissionRule(Broker broker, MasterDtos.BrokerRequest request) {
    if (request.brokerCommissionRate() == null || request.brokerCommissionType() == null) {
      return;
    }
    BrokerCommissionType type = parseBrokerCommissionType(request.brokerCommissionType());
    if (type != BrokerCommissionType.PERCENT) {
      return;
    }
    BrokerCommissionRule rule = brokerCommissionRuleRepository.findFirstByBrokerId(broker.getId())
        .orElseGet(() -> {
          BrokerCommissionRule fresh = new BrokerCommissionRule();
          fresh.setBroker(broker);
          return fresh;
        });
    rule.setRatePercent(request.brokerCommissionRate());
    brokerCommissionRuleRepository.save(rule);
  }

  private BrokerCommissionType parseBrokerCommissionType(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return BrokerCommissionType.valueOf(value.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }

  private BrokeragePaidBy parseBrokeragePaidBy(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return BrokeragePaidBy.valueOf(value.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      return null;
    }
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
}
