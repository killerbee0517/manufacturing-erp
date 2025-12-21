package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.TdsRule;
import com.manufacturing.erp.dto.MasterDtos;
import com.manufacturing.erp.repository.TdsRuleRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tds-rules")
public class TdsRuleController {
  private final TdsRuleRepository tdsRuleRepository;

  public TdsRuleController(TdsRuleRepository tdsRuleRepository) {
    this.tdsRuleRepository = tdsRuleRepository;
  }

  @GetMapping
  public List<MasterDtos.TdsRuleResponse> list() {
    return tdsRuleRepository.findAll().stream()
        .map(rule -> new MasterDtos.TdsRuleResponse(
            rule.getId(),
            rule.getSectionCode(),
            rule.getRatePercent(),
            rule.getThresholdAmount(),
            rule.getEffectiveFrom(),
            rule.getEffectiveTo()))
        .toList();
  }

  @PostMapping
  public MasterDtos.TdsRuleResponse create(@Valid @RequestBody MasterDtos.TdsRuleRequest request) {
    TdsRule rule = new TdsRule();
    rule.setSectionCode(request.sectionCode());
    rule.setRatePercent(request.ratePercent());
    rule.setThresholdAmount(request.thresholdAmount());
    rule.setEffectiveFrom(request.effectiveFrom());
    rule.setEffectiveTo(request.effectiveTo());
    TdsRule saved = tdsRuleRepository.save(rule);
    return new MasterDtos.TdsRuleResponse(
        saved.getId(),
        saved.getSectionCode(),
        saved.getRatePercent(),
        saved.getThresholdAmount(),
        saved.getEffectiveFrom(),
        saved.getEffectiveTo());
  }
}
