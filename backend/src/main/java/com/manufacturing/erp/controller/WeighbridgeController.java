package com.manufacturing.erp.controller;

import com.manufacturing.erp.dto.WeighbridgeDtos;
import com.manufacturing.erp.repository.WeighbridgeTicketRepository;
import com.manufacturing.erp.service.WeighbridgeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weighbridge")
public class WeighbridgeController {
  private final WeighbridgeService weighbridgeService;
  private final WeighbridgeTicketRepository ticketRepository;

  public WeighbridgeController(WeighbridgeService weighbridgeService,
                               WeighbridgeTicketRepository ticketRepository) {
    this.weighbridgeService = weighbridgeService;
    this.ticketRepository = ticketRepository;
  }

  @GetMapping("/tickets")
  public List<WeighbridgeDtos.TicketResponse> list() {
    return ticketRepository.findAll().stream()
        .map(ticket -> new WeighbridgeDtos.TicketResponse(
            ticket.getId(),
            ticket.getTicketNo(),
            ticket.getGrossWeight(),
            ticket.getTareWeight(),
            ticket.getNetWeight()))
        .toList();
  }

  @PostMapping("/tickets")
  public WeighbridgeDtos.TicketResponse create(@Valid @RequestBody WeighbridgeDtos.CreateTicketRequest request) {
    var ticket = weighbridgeService.createTicket(request);
    return new WeighbridgeDtos.TicketResponse(ticket.getId(), ticket.getTicketNo(), ticket.getGrossWeight(),
        ticket.getTareWeight(), ticket.getNetWeight());
  }
}
