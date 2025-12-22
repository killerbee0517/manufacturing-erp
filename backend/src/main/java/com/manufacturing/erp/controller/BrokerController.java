package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Broker;
import com.manufacturing.erp.dto.MasterDtos;
import com.manufacturing.erp.repository.BrokerRepository;
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

@RestController
@RequestMapping("/api/brokers")
public class BrokerController {
  private final BrokerRepository brokerRepository;

  public BrokerController(BrokerRepository brokerRepository) {
    this.brokerRepository = brokerRepository;
  }

  @GetMapping
  public List<MasterDtos.BrokerResponse> list(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) Integer limit) {
    List<Broker> brokers = (q == null || q.isBlank())
        ? brokerRepository.findAll()
        : brokerRepository.findByNameContainingIgnoreCaseOrCodeContainingIgnoreCase(q, q);
    return applyLimit(brokers, limit).stream()
        .map(broker -> new MasterDtos.BrokerResponse(broker.getId(), broker.getName(), broker.getCode()))
        .toList();
  }

  @GetMapping("/{id}")
  public MasterDtos.BrokerResponse get(@PathVariable Long id) {
    Broker broker = brokerRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Broker not found"));
    return new MasterDtos.BrokerResponse(broker.getId(), broker.getName(), broker.getCode());
  }

  @PostMapping
  public MasterDtos.BrokerResponse create(@Valid @RequestBody MasterDtos.BrokerRequest request) {
    Broker broker = new Broker();
    broker.setName(request.name());
    broker.setCode(request.code());
    Broker saved = brokerRepository.save(broker);
    return new MasterDtos.BrokerResponse(saved.getId(), saved.getName(), saved.getCode());
  }

  @PutMapping("/{id}")
  public MasterDtos.BrokerResponse update(@PathVariable Long id, @Valid @RequestBody MasterDtos.BrokerRequest request) {
    Broker broker = brokerRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Broker not found"));
    broker.setName(request.name());
    broker.setCode(request.code());
    Broker saved = brokerRepository.save(broker);
    return new MasterDtos.BrokerResponse(saved.getId(), saved.getName(), saved.getCode());
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
}
