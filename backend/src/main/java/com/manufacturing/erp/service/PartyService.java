package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Bank;
import com.manufacturing.erp.domain.Company;
import com.manufacturing.erp.domain.Customer;
import com.manufacturing.erp.domain.Enums.LedgerType;
import com.manufacturing.erp.domain.Enums.BrokerCommissionType;
import com.manufacturing.erp.domain.Enums.PayablePartyType;
import com.manufacturing.erp.domain.Enums.PartyRoleType;
import com.manufacturing.erp.domain.Enums.PartyStatus;
import com.manufacturing.erp.domain.Broker;
import com.manufacturing.erp.domain.BrokerCommissionRule;
import com.manufacturing.erp.domain.ExpenseParty;
import com.manufacturing.erp.domain.Party;
import com.manufacturing.erp.domain.PartyBankAccount;
import com.manufacturing.erp.domain.PartyRole;
import com.manufacturing.erp.domain.Supplier;
import com.manufacturing.erp.dto.PartyDtos;
import com.manufacturing.erp.repository.BankRepository;
import com.manufacturing.erp.repository.BrokerCommissionRuleRepository;
import com.manufacturing.erp.repository.BrokerRepository;
import com.manufacturing.erp.repository.CompanyRepository;
import com.manufacturing.erp.repository.CustomerRepository;
import com.manufacturing.erp.repository.ExpensePartyRepository;
import com.manufacturing.erp.repository.PartyBankAccountRepository;
import com.manufacturing.erp.repository.PartyRepository;
import com.manufacturing.erp.repository.PartyRoleRepository;
import com.manufacturing.erp.repository.SupplierRepository;
import com.manufacturing.erp.security.CompanyContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
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
  private final PartyBankAccountRepository partyBankAccountRepository;
  private final BankRepository bankRepository;
  private final SupplierRepository supplierRepository;
  private final CustomerRepository customerRepository;
  private final BrokerRepository brokerRepository;
  private final ExpensePartyRepository expensePartyRepository;
  private final BrokerCommissionRuleRepository brokerCommissionRuleRepository;
  private final CompanyRepository companyRepository;
  private final CompanyContext companyContext;
  private final LedgerAccountService ledgerAccountService;

  public PartyService(PartyRepository partyRepository, PartyRoleRepository partyRoleRepository,
                      PartyBankAccountRepository partyBankAccountRepository, BankRepository bankRepository,
                      SupplierRepository supplierRepository, CustomerRepository customerRepository,
                      BrokerRepository brokerRepository, ExpensePartyRepository expensePartyRepository,
                      BrokerCommissionRuleRepository brokerCommissionRuleRepository,
                      CompanyRepository companyRepository,
                      CompanyContext companyContext, LedgerAccountService ledgerAccountService) {
    this.partyRepository = partyRepository;
    this.partyRoleRepository = partyRoleRepository;
    this.partyBankAccountRepository = partyBankAccountRepository;
    this.bankRepository = bankRepository;
    this.supplierRepository = supplierRepository;
    this.customerRepository = customerRepository;
    this.brokerRepository = brokerRepository;
    this.expensePartyRepository = expensePartyRepository;
    this.brokerCommissionRuleRepository = brokerCommissionRuleRepository;
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
    syncRoleEntities(saved, request.roles());
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
    syncRoleEntities(saved, request.roles());
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

  public List<PartyBankAccount> listBankAccounts(Long partyId) {
    Company company = requireCompany();
    Party party = getPartyForCompany(partyId, company);
    return partyBankAccountRepository.findByCompanyIdAndPartyId(company.getId(), party.getId());
  }

  @Transactional(readOnly = true)
  public List<PartyDtos.PartyBankAccountSummaryResponse> listAllBankAccounts(String search) {
    Company company = requireCompany();
    List<PartyBankAccount> accounts = partyBankAccountRepository.findByCompanyId(company.getId());
    Stream<PartyBankAccount> stream = accounts.stream();
    if (search != null && !search.isBlank()) {
      String term = search.toLowerCase(Locale.ROOT);
      stream = stream.filter((account) -> {
        Party party = account.getParty();
        String partyName = party != null ? party.getName() : "";
        return containsIgnoreCase(partyName, term)
            || containsIgnoreCase(account.getBankName(), term)
            || containsIgnoreCase(account.getAccountNo(), term)
            || containsIgnoreCase(account.getIfsc(), term)
            || containsIgnoreCase(account.getBranch(), term);
      });
    }
    return stream.map((account) -> {
      Party party = account.getParty();
      return new PartyDtos.PartyBankAccountSummaryResponse(
          account.getId(),
          party != null ? party.getId() : null,
          party != null ? party.getName() : null,
          account.getBankName(),
          account.getBranch(),
          account.getAccountNo(),
          account.getIfsc(),
          account.getSwiftCode(),
          account.getAccountType(),
          account.isDefault(),
          account.isActive());
    }).toList();
  }

  @Transactional
  public PartyBankAccount addBankAccount(Long partyId, PartyDtos.PartyBankAccountRequest request) {
    Company company = requireCompany();
    Party party = getPartyForCompany(partyId, company);
    PartyBankAccount account = new PartyBankAccount();
    account.setCompany(company);
    account.setParty(party);
    applyBankAccountFields(account, request);
    PartyBankAccount saved = partyBankAccountRepository.save(account);
    if (saved.isDefault()) {
      unsetOtherDefaults(saved);
    }
    return saved;
  }

  @Transactional
  public PartyBankAccount updateBankAccount(Long partyId, Long accountId, PartyDtos.PartyBankAccountRequest request) {
    Company company = requireCompany();
    Party party = getPartyForCompany(partyId, company);
    PartyBankAccount account = partyBankAccountRepository.findById(accountId)
        .filter(a -> a.getParty().getId().equals(party.getId()))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank account not found"));
    applyBankAccountFields(account, request);
    PartyBankAccount saved = partyBankAccountRepository.save(account);
    if (saved.isDefault()) {
      unsetOtherDefaults(saved);
    }
    return saved;
  }

  @Transactional
  public void deleteBankAccount(Long partyId, Long accountId) {
    Company company = requireCompany();
    Party party = getPartyForCompany(partyId, company);
    PartyBankAccount account = partyBankAccountRepository.findById(accountId)
        .filter(a -> a.getParty().getId().equals(party.getId()))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank account not found"));
    partyBankAccountRepository.delete(account);
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
    List<PartyRoleType> requestedTypes = new ArrayList<>();
    List<PartyRole> toPersist = new ArrayList<>();
    if (roles != null) {
      for (PartyDtos.PartyRoleRequest roleRequest : roles) {
        requestedTypes.add(roleRequest.roleType());
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
        } else if (role.getRoleType() == PartyRoleType.EXPENSE) {
          ledgerAccountService.ensurePartyLedger(company, party, LedgerType.EXPENSE, party.getName());
        }
      }
    }
    if (roles != null) {
      for (PartyRole role : existing) {
        if (!requestedTypes.contains(role.getRoleType()) && role.isActive()) {
          role.setActive(false);
          toPersist.add(role);
        }
      }
    }
    partyRoleRepository.saveAll(toPersist);
  }

  private void syncRoleEntities(Party party, List<PartyDtos.PartyRoleRequest> roles) {
    if (roles == null) {
      return;
    }
    for (PartyDtos.PartyRoleRequest roleRequest : roles) {
      if (roleRequest.active() != null && !roleRequest.active()) {
        continue;
      }
      if (roleRequest.roleType() == PartyRoleType.SUPPLIER) {
        upsertSupplier(party, roleRequest);
      } else if (roleRequest.roleType() == PartyRoleType.CUSTOMER) {
        upsertCustomer(party, roleRequest);
      } else if (roleRequest.roleType() == PartyRoleType.BROKER) {
        upsertBroker(party, roleRequest);
      } else if (roleRequest.roleType() == PartyRoleType.EXPENSE) {
        upsertExpenseParty(party);
      }
    }
  }

  private void upsertSupplier(Party party, PartyDtos.PartyRoleRequest roleRequest) {
    Supplier supplier = supplierRepository.findByPartyId(party.getId()).orElseGet(Supplier::new);
    supplier.setParty(party);
    supplier.setName(party.getName());
    if (supplier.getCode() == null || supplier.getCode().isBlank()) {
      supplier.setCode(party.getPartyCode());
    }
    supplier.setAddress(party.getAddress());
    supplier.setState(party.getState());
    supplier.setCountry(party.getCountry());
    supplier.setPinCode(party.getPinCode());
    supplier.setPan(party.getPan());
    supplier.setGstNo(party.getGstNo());
    supplier.setContact(party.getContact());
    supplier.setEmail(party.getEmail());
    supplier.setBank(party.getBank());
    if (roleRequest.creditPeriodDays() != null) {
      supplier.setCreditPeriod(roleRequest.creditPeriodDays());
    }
    supplierRepository.save(supplier);
  }

  private void upsertCustomer(Party party, PartyDtos.PartyRoleRequest roleRequest) {
    Customer customer = customerRepository.findByPartyId(party.getId()).orElseGet(Customer::new);
    customer.setParty(party);
    customer.setName(party.getName());
    if (customer.getCode() == null || customer.getCode().isBlank()) {
      customer.setCode(party.getPartyCode());
    }
    customer.setAddress(party.getAddress());
    customer.setState(party.getState());
    customer.setCountry(party.getCountry());
    customer.setPinCode(party.getPinCode());
    customer.setPan(party.getPan());
    customer.setGstNo(party.getGstNo());
    customer.setContact(party.getContact());
    customer.setEmail(party.getEmail());
    customer.setBank(party.getBank());
    if (roleRequest.creditPeriodDays() != null) {
      customer.setCreditPeriod(roleRequest.creditPeriodDays());
    }
    customerRepository.save(customer);
  }

  private void upsertBroker(Party party, PartyDtos.PartyRoleRequest roleRequest) {
    Broker broker = brokerRepository.findByPartyId(party.getId()).orElseGet(Broker::new);
    broker.setParty(party);
    broker.setName(party.getName());
    if (broker.getCode() == null || broker.getCode().isBlank()) {
      broker.setCode(party.getPartyCode());
    }
    Broker saved = brokerRepository.save(broker);
    if (roleRequest.brokerCommissionType() == BrokerCommissionType.PERCENT
        && roleRequest.brokerCommissionRate() != null) {
      BrokerCommissionRule rule = brokerCommissionRuleRepository.findFirstByBrokerId(saved.getId())
          .orElseGet(() -> {
            BrokerCommissionRule fresh = new BrokerCommissionRule();
            fresh.setBroker(saved);
            return fresh;
          });
      rule.setRatePercent(roleRequest.brokerCommissionRate());
      brokerCommissionRuleRepository.save(rule);
    }
  }

  private void upsertExpenseParty(Party party) {
    ExpenseParty expenseParty = expensePartyRepository.findByPartyId(party.getId()).orElseGet(ExpenseParty::new);
    expenseParty.setParty(party);
    expenseParty.setName(party.getName());
    expenseParty.setPartyType(PayablePartyType.EXPENSE);
    expensePartyRepository.save(expenseParty);
  }

  private PartyRoleType parseRole(String role) {
    try {
      return PartyRoleType.valueOf(role.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid party role: " + role);
    }
  }

  private Party getPartyForCompany(Long partyId, Company company) {
    return partyRepository.findById(partyId)
        .filter(party -> party.getCompany().getId().equals(company.getId()))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Party not found"));
  }

  private void applyBankAccountFields(PartyBankAccount account, PartyDtos.PartyBankAccountRequest request) {
    account.setBankName(request.bankName());
    account.setBranch(request.branch());
    account.setAccountNo(request.accountNo());
    account.setIfsc(request.ifsc());
    account.setSwiftCode(request.swiftCode());
    account.setAccountType(request.accountType());
    account.setDefault(request.isDefault() != null && request.isDefault());
    account.setActive(request.active() == null || request.active());
  }

  private void unsetOtherDefaults(PartyBankAccount account) {
    List<PartyBankAccount> accounts = partyBankAccountRepository.findByCompanyIdAndPartyId(
        account.getCompany().getId(), account.getParty().getId());
    for (PartyBankAccount other : accounts) {
      if (!other.getId().equals(account.getId()) && other.isDefault()) {
        other.setDefault(false);
      }
    }
    partyBankAccountRepository.saveAll(accounts);
  }

  private boolean containsIgnoreCase(String value, String term) {
    if (value == null) {
      return false;
    }
    return value.toLowerCase(Locale.ROOT).contains(term);
  }
}
