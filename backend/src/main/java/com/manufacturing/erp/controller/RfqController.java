package com.manufacturing.erp.controller;

import com.manufacturing.erp.dto.TransactionDtos;
import com.manufacturing.erp.service.RfqService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rfq")
public class RfqController {
  private final RfqService rfqService;

  public RfqController(RfqService rfqService) {
    this.rfqService = rfqService;
  }

  @GetMapping
  public Page<TransactionDtos.RfqResponse> list(@RequestParam(required = false) String q,
                                                @RequestParam(required = false) String status,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size,
                                                @RequestParam(defaultValue = "id,desc") String sort) {
    PageRequest pageRequest = PageRequest.of(page, size, parseSort(sort));
    return rfqService.list(q, status, pageRequest);
  }

  @GetMapping("/{id}")
  public TransactionDtos.RfqResponse getById(@PathVariable Long id) {
    return rfqService.getById(id);
  }

  @PostMapping
  public TransactionDtos.RfqResponse create(@Valid @RequestBody TransactionDtos.RfqRequest request) {
    return rfqService.create(request);
  }

  @PutMapping("/{id}")
  public TransactionDtos.RfqResponse update(@PathVariable Long id, @Valid @RequestBody TransactionDtos.RfqRequest request) {
    return rfqService.update(id, request);
  }

  @PostMapping("/{id}/submit")
  public TransactionDtos.RfqResponse submit(@PathVariable Long id) {
    return rfqService.submit(id);
  }

  @PostMapping("/{id}/approve")
  public TransactionDtos.RfqResponse approve(@PathVariable Long id) {
    return rfqService.approve(id);
  }

  @PostMapping("/{id}/award")
  public TransactionDtos.RfqResponse award(@PathVariable Long id, @Valid @RequestBody TransactionDtos.RfqAwardRequest request) {
    return rfqService.award(id, request);
  }

  @PostMapping("/{id}/reject")
  public TransactionDtos.RfqResponse reject(@PathVariable Long id, @RequestBody(required = false) String remarks) {
    return rfqService.reject(id, remarks);
  }

  @PostMapping("/{id}/close")
  public TransactionDtos.RfqCloseResponse close(@PathVariable Long id,
                                                @Valid @RequestBody TransactionDtos.RfqCloseRequest request) {
    return rfqService.close(id, request);
  }

  private Sort parseSort(String sort) {
    String[] parts = sort.split(",");
    String field = parts.length > 0 ? parts[0] : "id";
    String direction = parts.length > 1 ? parts[1] : "desc";
    return "asc".equalsIgnoreCase(direction) ? Sort.by(field).ascending() : Sort.by(field).descending();
  }
}
