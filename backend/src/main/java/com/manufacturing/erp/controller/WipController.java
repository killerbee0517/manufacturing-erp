package com.manufacturing.erp.controller;

import com.manufacturing.erp.dto.ProductionDtos;
import com.manufacturing.erp.service.ProductionRunService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class WipController {
  private final ProductionRunService productionRunService;

  public WipController(ProductionRunService productionRunService) {
    this.productionRunService = productionRunService;
  }

  @GetMapping("/wip")
  public List<ProductionDtos.WipSelectionResponse> search(@RequestParam(required = false) String search,
                                                          @RequestParam(required = false) String q) {
    String resolved = (search != null && !search.isBlank()) ? search : q;
    return productionRunService.searchWip(resolved);
  }
}
