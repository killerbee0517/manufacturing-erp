package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Customer;
import com.manufacturing.erp.dto.MasterDtos;
import com.manufacturing.erp.repository.BankRepository;
import com.manufacturing.erp.repository.CustomerRepository;
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

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
  private final CustomerRepository customerRepository;
  private final BankRepository bankRepository;

  public CustomerController(CustomerRepository customerRepository, BankRepository bankRepository) {
    this.customerRepository = customerRepository;
    this.bankRepository = bankRepository;
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

  @PostMapping
  public MasterDtos.CustomerResponse create(@Valid @RequestBody MasterDtos.CustomerRequest request) {
    Customer customer = new Customer();
    applyRequest(customer, request);
    Customer saved = customerRepository.save(customer);
    return toResponse(saved);
  }

  @PutMapping("/{id}")
  public MasterDtos.CustomerResponse update(@PathVariable Long id, @Valid @RequestBody MasterDtos.CustomerRequest request) {
    Customer customer = customerRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    applyRequest(customer, request);
    Customer saved = customerRepository.save(customer);
    return toResponse(saved);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    if (!customerRepository.existsById(id)) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
    }
    customerRepository.deleteById(id);
  }

  private void applyRequest(Customer customer, MasterDtos.CustomerRequest request) {
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
        customer.getCreditPeriod());
  }

  private List<Customer> applyLimit(List<Customer> customers, Integer limit) {
    if (limit == null) {
      return customers;
    }
    return customers.stream().limit(limit).toList();
  }
}
