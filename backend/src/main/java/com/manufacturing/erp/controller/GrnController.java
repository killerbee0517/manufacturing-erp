package com.manufacturing.erp.controller;

import com.manufacturing.erp.dto.GrnDtos;
import com.manufacturing.erp.service.GrnService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/grn")
public class GrnController {
  private final GrnService grnService;

  public GrnController(GrnService grnService) {
    this.grnService = grnService;
  }

  @PostMapping
  public GrnDtos.GrnResponse create(@Valid @RequestBody GrnDtos.CreateGrnRequest request) {
    var grn = grnService.createGrn(request);
    return new GrnDtos.GrnResponse(grn.getId(), grn.getGrnNo(), grn.getStatus().name());
  }
}
