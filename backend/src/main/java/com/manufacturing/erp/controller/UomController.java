package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Uom;
import com.manufacturing.erp.repository.UomRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/uoms")
public class UomController {
  private final UomRepository uomRepository;

  public UomController(UomRepository uomRepository) {
    this.uomRepository = uomRepository;
  }

  @GetMapping
  public List<UomResponse> list() {
    return uomRepository.findAll().stream()
        .map(uom -> new UomResponse(uom.getId(), uom.getCode(), uom.getDescription()))
        .toList();
  }

  public record UomResponse(Long id, String code, String description) {}
}
