package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Bank;
import com.manufacturing.erp.domain.Company;
import com.manufacturing.erp.domain.Enums.LedgerType;
import com.manufacturing.erp.domain.Enums.PartyRoleType;
import com.manufacturing.erp.domain.Enums.PartyStatus;
import com.manufacturing.erp.domain.Party;
import com.manufacturing.erp.domain.PartyRole;
import com.manufacturing.erp.dto.PartyDtos;
import com.manufacturing.erp.repository.BankRepository;
import com.manufacturing.erp.repository.CompanyRepository;
import com.manufacturing.erp.repository.PartyRepository;
import com.manufacturing.erp.repository.PartyRoleRepository;
import com.manufacturing.erp.security.CompanyContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class PartyService {
  private final PartyRepository partyRepository;
  private final PartyRoleRepository partyRoleRepository;
  private final BankRepository bankRepository;
  private final CompanyRepository companyRepository;
  private final CompanyContext companyContext;
  private final LedgerAccountService ledgerAccountService;

  public PartyService(PartyRepository partyRepository, PartyRoleRepository partyRoleRepository,
                      BankRepository bankRepository, CompanyRepository companyRepository,
                      CompanyContext companyContext, LedgerAccountService ledgerAccountService) {
    this.partyRepository = partyRepository;
    this.partyRoleRepository = partyRoleRepository;
    this.bankRepository = bankRepository;
    this.companyRepository = companyRepository;
    this.companyContext = companyContext;
    this.ledgerAccountService = ledgerAccountService;
  }

  private Company requireCompany() {
    Long companyId = companyContext.getCompanyId();
    if (companyId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing company context");
    }
    return companyRepository.findById(companyId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
  }

  @Transactional
  public Party create(PartyDtos.PartyRequest request) {
    Company company = requireCompany();
    Party party = new Party();
    party.setCompany(company);
    applyPartyFields(party, request, company);
    Party saved = partyRepository.save(party);
    syncRoles(saved, request.roles(), company);
    return saved;
  }

  @Transactional
  public Party update(Long id, PartyDtos.PartyRequest request) {
    Company company = requireCompany();
    Party party = partyRepository.findById(id)
        .filter(p -> p.getCompany().getId().equals(company.getId()))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Party not found"));
    applyPartyFields(party, request, company);
    Party saved = partyRepository.save(party);
    syncRoles(saved, request.roles(), company);
    return saved;
  }

  public Page<Party> search(String role, String search, Pageable pageable) {
    Company company = requireCompany();
    if (role != null && !role.isBlank()) {
      return partyRepository.searchByRole(company.getId(), parseRole(role), search, pageable);
    }
    return partyRepository.search(company.getId(), search, pageable);
  }

  public List<Party> autocomplete(String role, String search, Pageable pageable) {
    Company company = requireCompany();
    if (role != null && !role.isBlank()) {
      return partyRepository.autocompleteByRole(company.getId(), parseRole(role), search, pageable);
    }
    return partyRepository.autocomplete(company.getId(), search, pageable);
  }

  public Party get(Long id) {
    Company company = requireCompany();
    return partyRepository.findById(id)
        .filter(p -> p.getCompany().getId().equals(company.getId()))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Party not found"));
  }

  public List<PartyRole> getRolesForParty(Party party) {
    Company company = requireCompany();
    return partyRoleRepository.findByCompanyIdAndPartyId(company.getId(), party.getId());
  }

  private void applyPartyFields(Party party, PartyDtos.PartyRequest request, Company company) {
    String code = request.partyCode();
    if (code == null || code.isBlank()) {
      code = "PTY-" + (partyRepository.countByCompanyId(company.getId()) + 1);
    }
    party.setPartyCode(code);
    party.setName(request.name());
    party.setAddress(request.address());
    party.setState(request.state());
    party.setCountry(request.country());
    party.setPinCode(request.pinCode());
    party.setPan(request.pan());
    party.setGstNo(request.gstNo());
    party.setContact(request.contact());
    party.setEmail(request.email());
    if (request.status() != null) {
      party.setStatus(request.status());
    } else if (party.getStatus() == null) {
      party.setStatus(PartyStatus.ACTIVE);
    }
    if (request.bankId() != null) {
      Bank bank = bankRepository.findById(request.bankId())
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank not found"));
      if (bank.getCompany() != null && !bank.getCompany().getId().equals(company.getId())) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bank belongs to another company");
      }
      party.setBank(bank);
    } else {
      party.setBank(null);
    }
  }

  private void syncRoles(Party party, List<PartyDtos.PartyRoleRequest> roles, Company company) {
    List<PartyRole> existing = partyRoleRepository.findByCompanyIdAndPartyId(company.getId(), party.getId());
    List<PartyRole> toPersist = new ArrayList<>();
    if (roles != null) {
      for (PartyDtos.PartyRoleRequest roleRequest : roles) {
        PartyRole role = existing.stream()
            .filter(r -> r.getRoleType() == roleRequest.roleType())
            .findFirst()
            .orElseGet(() -> {
              PartyRole fresh = new PartyRole();
              fresh.setCompany(company);
              fresh.setParty(party);
              fresh.setRoleType(roleRequest.roleType());
              return fresh;
            });
        role.setCreditPeriodDays(roleRequest.creditPeriodDays());
        role.setSupplierType(roleRequest.supplierType());
        role.setBrokerCommissionType(roleRequest.brokerCommissionType());
        role.setBrokerCommissionRate(roleRequest.brokerCommissionRate());
        role.setBrokeragePaidBy(roleRequest.brokeragePaidBy() != null ? roleRequest.brokeragePaidBy() : com.manufacturing.erp.domain.Enums.BrokeragePaidBy.COMPANY);
        role.setActive(roleRequest.active() == null || roleRequest.active());
        toPersist.add(role);
        if (role.getRoleType() == PartyRoleType.SUPPLIER) {
          ledgerAccountService.ensurePartyLedger(company, party, LedgerType.SUPPLIER, party.getName());
        } else if (role.getRoleType() == PartyRoleType.CUSTOMER) {
          ledgerAccountService.ensurePartyLedger(company, party, LedgerType.CUSTOMER, party.getName());
        } else if (role.getRoleType() == PartyRoleType.BROKER) {
          ledgerAccountService.ensurePartyLedger(company, party, LedgerType.BROKER, party.getName());
        }
      }
    }
    partyRoleRepository.saveAll(toPersist);
  }

  private PartyRoleType parseRole(String role) {
    try {
      return PartyRoleType.valueOf(role.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid party role: " + role);
    }
  }
}
