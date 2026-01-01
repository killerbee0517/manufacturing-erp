package com.manufacturing.erp.controller;

import com.manufacturing.erp.dto.WeighbridgeDtos;
import com.manufacturing.erp.service.WeighbridgeService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weighbridge")
public class WeighbridgeController {
  private final WeighbridgeService weighbridgeService;

  public WeighbridgeController(WeighbridgeService weighbridgeService) {
    this.weighbridgeService = weighbridgeService;
  }

  @GetMapping("/tickets")
  public List<WeighbridgeDtos.TicketResponse> list(@RequestParam(required = false) String status) {
    return weighbridgeService.list(status).stream().map(this::toResponse).toList();
  }

  @GetMapping("/tickets/{id}")
  public WeighbridgeDtos.TicketResponse get(@PathVariable Long id) {
    return toResponse(weighbridgeService.getById(id));
  }

  @PostMapping("/tickets")
  public WeighbridgeDtos.TicketResponse create(@Valid @RequestBody WeighbridgeDtos.CreateTicketRequest request) {
    var ticket = weighbridgeService.createTicket(request);
    return toResponse(ticket);
  }

  @PutMapping("/tickets/{id}")
  public WeighbridgeDtos.TicketResponse update(@PathVariable Long id,
                                               @Valid @RequestBody WeighbridgeDtos.CreateTicketRequest request) {
    var ticket = weighbridgeService.updateTicket(id, request);
    return toResponse(ticket);
  }

  @PutMapping("/tickets/{id}/unload")
  public WeighbridgeDtos.TicketResponse unload(@PathVariable Long id,
                                               @Valid @RequestBody WeighbridgeDtos.UnloadTicketRequest request) {
    var ticket = weighbridgeService.unload(id, request);
    return toResponse(ticket);
  }

  private WeighbridgeDtos.TicketResponse toResponse(com.manufacturing.erp.domain.WeighbridgeTicket ticket) {
    return new WeighbridgeDtos.TicketResponse(
        ticket.getId(),
        ticket.getSerialNo(),
        ticket.getVehicle() != null ? ticket.getVehicle().getId() : null,
        ticket.getPurchaseOrder() != null ? ticket.getPurchaseOrder().getId() : null,
        ticket.getSupplier() != null ? ticket.getSupplier().getId() : null,
        ticket.getItem() != null ? ticket.getItem().getId() : null,
        ticket.getDateIn(),
        ticket.getTimeIn(),
        ticket.getSecondDate(),
        ticket.getSecondTime(),
        ticket.getGrossWeight(),
        ticket.getUnloadedWeight(),
        ticket.getNetWeight(),
        ticket.getStatus() != null ? ticket.getStatus().name() : null);
  }
}
