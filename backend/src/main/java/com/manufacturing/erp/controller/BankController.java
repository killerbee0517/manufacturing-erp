package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Bank;
import com.manufacturing.erp.domain.Company;
import com.manufacturing.erp.dto.BankDtos;
import com.manufacturing.erp.repository.BankRepository;
import com.manufacturing.erp.repository.CompanyRepository;
import com.manufacturing.erp.security.CompanyContext;
import com.manufacturing.erp.service.LedgerAccountService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
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

@RestController
@RequestMapping("/api/banks")
public class BankController {
  private final BankRepository bankRepository;
  private final CompanyRepository companyRepository;
  private final CompanyContext companyContext;
  private final LedgerAccountService ledgerAccountService;

  public BankController(BankRepository bankRepository, CompanyRepository companyRepository, CompanyContext companyContext,
                        LedgerAccountService ledgerAccountService) {
    this.bankRepository = bankRepository;
    this.companyRepository = companyRepository;
    this.companyContext = companyContext;
    this.ledgerAccountService = ledgerAccountService;
  }

  @GetMapping
  public Page<BankDtos.BankResponse> list(
      @RequestParam(required = false) String search,
      Pageable pageable) {
    Company company = requireCompany();
    Page<Bank> page = bankRepository.search(company.getId(), search, pageable);
    return page.map(this::toResponse);
  }

  @GetMapping("/autocomplete")
  public List<BankDtos.BankOption> autocomplete(
      @RequestParam(required = false) String q,
      @RequestParam(defaultValue = "20") int limit) {
    Company company = requireCompany();
    Pageable pageable = PageRequest.of(0, limit);
    return bankRepository.autocomplete(company.getId(), q, pageable).stream()
        .map(bank -> new BankDtos.BankOption(bank.getId(), bank.getName()))
        .toList();
  }

  @GetMapping("/{id}")
  public BankDtos.BankResponse get(@PathVariable Long id) {
    Company company = requireCompany();
    Bank bank = bankRepository.findById(id)
        .filter(b -> b.getCompany() == null || b.getCompany().getId().equals(company.getId()))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank not found"));
    return toResponse(bank);
  }

  @PostMapping
  @Transactional
  public BankDtos.BankResponse create(@Valid @RequestBody BankDtos.BankRequest request) {
    Company company = requireCompany();
    Bank bank = new Bank();
    bank.setCompany(company);
    applyRequest(bank, request);
    Bank saved = bankRepository.save(bank);
    ledgerAccountService.ensureBankLedger(company, saved);
    return toResponse(saved);
  }

  @PutMapping("/{id}")
  @Transactional
  public BankDtos.BankResponse update(@PathVariable Long id, @Valid @RequestBody BankDtos.BankRequest request) {
    Company company = requireCompany();
    Bank bank = bankRepository.findById(id)
        .filter(b -> b.getCompany() == null || b.getCompany().getId().equals(company.getId()))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank not found"));
    applyRequest(bank, request);
    if (bank.getCompany() == null) {
      bank.setCompany(company);
    }
    Bank saved = bankRepository.save(bank);
    ledgerAccountService.ensureBankLedger(company, saved);
    return toResponse(saved);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    Company company = requireCompany();
    Bank bank = bankRepository.findById(id)
        .filter(b -> b.getCompany() == null || b.getCompany().getId().equals(company.getId()))
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank not found"));
    bankRepository.delete(bank);
  }

  private void applyRequest(Bank bank, BankDtos.BankRequest request) {
    bank.setName(request.name());
    bank.setBranch(request.branch());
    bank.setAccNo(request.accNo());
    bank.setIfsc(request.ifsc());
    bank.setSwiftCode(request.swiftCode());
    bank.setType(request.type());
    if (request.active() != null) {
      bank.setActive(request.active());
    }
  }

  private BankDtos.BankResponse toResponse(Bank bank) {
    return new BankDtos.BankResponse(
        bank.getId(),
        bank.getName(),
        bank.getBranch(),
        bank.getAccNo(),
        bank.getIfsc(),
        bank.getSwiftCode(),
        bank.getType(),
        bank.isActive());
  }

  private Company requireCompany() {
    Long companyId = companyContext.getCompanyId();
    if (companyId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing company context");
    }
    return companyRepository.findById(companyId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
  }
}
