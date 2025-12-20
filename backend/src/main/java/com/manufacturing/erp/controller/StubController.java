package com.manufacturing.erp.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stubs")
public class StubController {

  @GetMapping("/rfq-quote-comparison")
  public Map<String, String> rfqQuoteComparison() {
    return Map.of("status", "TODO", "message", "RFQ quote comparison pending implementation");
  }

  @GetMapping("/inter-branch-reconciliation")
  public Map<String, String> interBranchReconciliation() {
    return Map.of("status", "TODO", "message", "Inter-branch reconciliation report pending implementation");
  }
}
