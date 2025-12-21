package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Rfq;
import com.manufacturing.erp.dto.TransactionDtos;
import com.manufacturing.erp.repository.RfqRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rfq")
public class RfqController {
  private final RfqRepository rfqRepository;

  public RfqController(RfqRepository rfqRepository) {
    this.rfqRepository = rfqRepository;
  }

  @GetMapping
  public List<TransactionDtos.RfqResponse> list() {
    return rfqRepository.findAll().stream()
        .map(rfq -> new TransactionDtos.RfqResponse(rfq.getId(), rfq.getRfqNo(), rfq.getStatus()))
        .toList();
  }

  @PostMapping
  public TransactionDtos.RfqResponse create(@Valid @RequestBody TransactionDtos.RfqRequest request) {
    Rfq rfq = new Rfq();
    rfq.setRfqNo(request.rfqNo());
    rfq.setStatus(request.status() != null ? request.status() : "DRAFT");
    Rfq saved = rfqRepository.save(rfq);
    return new TransactionDtos.RfqResponse(saved.getId(), saved.getRfqNo(), saved.getStatus());
  }
}
