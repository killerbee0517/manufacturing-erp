package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Bank;
import com.manufacturing.erp.dto.MasterDtos;
import com.manufacturing.erp.repository.BankRepository;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/banks")
public class BankController {
  private final BankRepository bankRepository;

  public BankController(BankRepository bankRepository) {
    this.bankRepository = bankRepository;
  }

  @GetMapping
  public List<MasterDtos.BankResponse> list(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) Integer limit) {
    List<Bank> banks = (q == null || q.isBlank())
        ? bankRepository.findAll()
        : bankRepository.findByNameContainingIgnoreCase(q);
    return applyLimit(banks, limit).stream().map(this::toResponse).toList();
  }

  @GetMapping("/{id}")
  public MasterDtos.BankResponse get(@PathVariable Long id) {
    Bank bank = bankRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank not found"));
    return toResponse(bank);
  }

  @PostMapping
  @Transactional
  public MasterDtos.BankResponse create(@Valid @RequestBody MasterDtos.BankRequest request) {
    Bank bank = new Bank();
    applyRequest(bank, request);
    Bank saved = bankRepository.save(bank);
    return toResponse(saved);
  }

  @PutMapping("/{id}")
  @Transactional
  public MasterDtos.BankResponse update(@PathVariable Long id, @Valid @RequestBody MasterDtos.BankRequest request) {
    Bank bank = bankRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank not found"));
    applyRequest(bank, request);
    Bank saved = bankRepository.save(bank);
    return toResponse(saved);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    if (!bankRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bank not found");
    }
    bankRepository.deleteById(id);
  }

  private void applyRequest(Bank bank, MasterDtos.BankRequest request) {
    bank.setName(request.name());
    bank.setBranch(request.branch());
    bank.setAccNo(request.accNo());
    bank.setIfsc(request.ifsc());
    bank.setSwiftCode(request.swiftCode());
    bank.setType(request.type());
  }

  private MasterDtos.BankResponse toResponse(Bank bank) {
    return new MasterDtos.BankResponse(
        bank.getId(),
        bank.getName(),
        bank.getBranch(),
        bank.getAccNo(),
        bank.getIfsc(),
        bank.getSwiftCode(),
        bank.getType());
  }

  private List<Bank> applyLimit(List<Bank> banks, Integer limit) {
    if (limit == null) {
      return banks;
    }
    return banks.stream().limit(limit).toList();
  }
}
