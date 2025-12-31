package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Company;
import com.manufacturing.erp.dto.CompanyDtos;
import com.manufacturing.erp.repository.CompanyRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {
  private final CompanyRepository companyRepository;

  public CompanyController(CompanyRepository companyRepository) {
    this.companyRepository = companyRepository;
  }

  @GetMapping("/my")
  public List<CompanyDtos.CompanyResponse> myCompanies(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
    }
    return companyRepository.findCompaniesForUser(authentication.getName()).stream()
        .map(this::toResponse)
        .toList();
  }

  @GetMapping
  public Page<CompanyDtos.CompanyResponse> list(Pageable pageable) {
    return companyRepository.findAll(pageable).map(this::toResponse);
  }

  @GetMapping("/{id}")
  public CompanyDtos.CompanyResponse get(@PathVariable Long id) {
    return companyRepository.findById(id)
        .map(this::toResponse)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
  }

  @PostMapping
  public CompanyDtos.CompanyResponse create(@Valid @RequestBody CompanyDtos.CompanyRequest request) {
    Company company = new Company();
    applyRequest(company, request);
    return toResponse(companyRepository.save(company));
  }

  @PutMapping("/{id}")
  public CompanyDtos.CompanyResponse update(@PathVariable Long id, @Valid @RequestBody CompanyDtos.CompanyRequest request) {
    Company company = companyRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
    applyRequest(company, request);
    return toResponse(companyRepository.save(company));
  }

  private void applyRequest(Company company, CompanyDtos.CompanyRequest request) {
    company.setCode(request.code());
    company.setName(request.name());
    company.setGstNo(request.gstNo());
    company.setPan(request.pan());
    company.setAddress(request.address());
    if (request.active() != null) {
      company.setActive(request.active());
    }
    if (request.parentCompanyId() != null) {
      Company parent = companyRepository.findById(request.parentCompanyId())
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent company not found"));
      company.setParent(parent);
    } else {
      company.setParent(null);
    }
  }

  private CompanyDtos.CompanyResponse toResponse(Company company) {
    return new CompanyDtos.CompanyResponse(
        company.getId(),
        company.getCode(),
        company.getName(),
        company.getGstNo(),
        company.getPan(),
        company.getAddress(),
        company.isActive(),
        company.getParent() != null ? company.getParent().getId() : null);
  }
}
