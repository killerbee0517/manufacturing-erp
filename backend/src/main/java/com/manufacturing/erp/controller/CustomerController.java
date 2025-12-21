package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Customer;
import com.manufacturing.erp.dto.MasterDtos;
import com.manufacturing.erp.repository.CustomerRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {
  private final CustomerRepository customerRepository;

  public CustomerController(CustomerRepository customerRepository) {
    this.customerRepository = customerRepository;
  }

  @GetMapping
  public List<MasterDtos.CustomerResponse> list() {
    return customerRepository.findAll().stream()
        .map(customer -> new MasterDtos.CustomerResponse(customer.getId(), customer.getName(), customer.getCode()))
        .toList();
  }

  @PostMapping
  public MasterDtos.CustomerResponse create(@Valid @RequestBody MasterDtos.CustomerRequest request) {
    Customer customer = new Customer();
    customer.setName(request.name());
    customer.setCode(request.code());
    Customer saved = customerRepository.save(customer);
    return new MasterDtos.CustomerResponse(saved.getId(), saved.getName(), saved.getCode());
  }
}
