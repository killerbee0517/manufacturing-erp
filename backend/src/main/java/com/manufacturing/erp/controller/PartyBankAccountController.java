package com.manufacturing.erp.controller;

import com.manufacturing.erp.dto.PartyDtos;
import com.manufacturing.erp.service.PartyService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/party-bank-accounts")
public class PartyBankAccountController {
  private final PartyService partyService;

  public PartyBankAccountController(PartyService partyService) {
    this.partyService = partyService;
  }

  @GetMapping
  public List<PartyDtos.PartyBankAccountSummaryResponse> list(
      @RequestParam(required = false) String search) {
    return partyService.listAllBankAccounts(search);
  }
}
