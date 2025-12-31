package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Party;
import com.manufacturing.erp.domain.PartyRole;
import com.manufacturing.erp.dto.PartyDtos;
import com.manufacturing.erp.service.PartyService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parties")
public class PartyController {
  private final PartyService partyService;

  public PartyController(PartyService partyService) {
    this.partyService = partyService;
  }

  @GetMapping
  public Page<PartyDtos.PartyResponse> list(
      @RequestParam(required = false) String role,
      @RequestParam(required = false) String search,
      Pageable pageable) {
    return partyService.search(role, search, pageable).map(this::toResponse);
  }

  @GetMapping("/autocomplete")
  public List<PartyDtos.PartyOption> autocomplete(
      @RequestParam(required = false) String role,
      @RequestParam(required = false) String q,
      @RequestParam(defaultValue = "20") int limit) {
    Pageable pageable = PageRequest.of(0, limit);
    return partyService.autocomplete(role, q, pageable).stream()
        .map(p -> new PartyDtos.PartyOption(p.getId(), p.getPartyCode(), p.getName()))
        .toList();
  }

  @GetMapping("/{id}")
  public PartyDtos.PartyResponse get(@PathVariable Long id) {
    return toResponse(partyService.get(id));
  }

  @PostMapping
  public PartyDtos.PartyResponse create(@Valid @RequestBody PartyDtos.PartyRequest request) {
    return toResponse(partyService.create(request));
  }

  @PutMapping("/{id}")
  public PartyDtos.PartyResponse update(@PathVariable Long id, @Valid @RequestBody PartyDtos.PartyRequest request) {
    return toResponse(partyService.update(id, request));
  }

  private PartyDtos.PartyResponse toResponse(Party party) {
    List<PartyRole> roles = party.getCompany() != null ? partyService.getRolesForParty(party) : List.of();
    return new PartyDtos.PartyResponse(
        party.getId(),
        party.getPartyCode(),
        party.getName(),
        party.getAddress(),
        party.getState(),
        party.getCountry(),
        party.getPinCode(),
        party.getPan(),
        party.getGstNo(),
        party.getContact(),
        party.getEmail(),
        party.getBank() != null ? party.getBank().getId() : null,
        party.getBank() != null ? party.getBank().getName() : null,
        party.getStatus(),
        roles.stream().map(this::toRoleResponse).toList());
  }

  private PartyDtos.PartyRoleResponse toRoleResponse(PartyRole role) {
    return new PartyDtos.PartyRoleResponse(
        role.getId(),
        role.getRoleType(),
        role.getCreditPeriodDays(),
        role.getSupplierType(),
        role.getBrokerCommissionType(),
        role.getBrokerCommissionRate(),
        role.getBrokeragePaidBy(),
        role.isActive());
  }
}
