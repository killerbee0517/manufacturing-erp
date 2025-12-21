package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Broker;
import com.manufacturing.erp.dto.MasterDtos;
import com.manufacturing.erp.repository.BrokerRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/brokers")
public class BrokerController {
  private final BrokerRepository brokerRepository;

  public BrokerController(BrokerRepository brokerRepository) {
    this.brokerRepository = brokerRepository;
  }

  @GetMapping
  public List<MasterDtos.BrokerResponse> list() {
    return brokerRepository.findAll().stream()
        .map(broker -> new MasterDtos.BrokerResponse(broker.getId(), broker.getName(), broker.getCode()))
        .toList();
  }

  @PostMapping
  public MasterDtos.BrokerResponse create(@Valid @RequestBody MasterDtos.BrokerRequest request) {
    Broker broker = new Broker();
    broker.setName(request.name());
    broker.setCode(request.code());
    Broker saved = brokerRepository.save(broker);
    return new MasterDtos.BrokerResponse(saved.getId(), saved.getName(), saved.getCode());
  }
}
