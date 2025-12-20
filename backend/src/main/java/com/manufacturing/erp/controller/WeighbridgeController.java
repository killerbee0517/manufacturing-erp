package com.manufacturing.erp.controller;

import com.manufacturing.erp.dto.WeighbridgeDtos;
import com.manufacturing.erp.service.WeighbridgeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weighbridge")
public class WeighbridgeController {
  private final WeighbridgeService weighbridgeService;

  public WeighbridgeController(WeighbridgeService weighbridgeService) {
    this.weighbridgeService = weighbridgeService;
  }

  @PostMapping("/tickets")
  public WeighbridgeDtos.TicketResponse create(@Valid @RequestBody WeighbridgeDtos.CreateTicketRequest request) {
    var ticket = weighbridgeService.createTicket(request);
    return new WeighbridgeDtos.TicketResponse(ticket.getId(), ticket.getTicketNo(), ticket.getGrossWeight(),
        ticket.getTareWeight(), ticket.getNetWeight());
  }
}
