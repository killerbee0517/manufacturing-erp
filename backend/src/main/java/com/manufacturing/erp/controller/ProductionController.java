package com.manufacturing.erp.controller;

import com.manufacturing.erp.dto.ProductionDtos;
import com.manufacturing.erp.service.ProductionRunService;
import com.manufacturing.erp.service.ProductionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/production")
public class ProductionController {
  private final ProductionService productionService;
  private final ProductionRunService productionRunService;

  public ProductionController(ProductionService productionService, ProductionRunService productionRunService) {
    this.productionService = productionService;
    this.productionRunService = productionRunService;
  }

  // BOM
  @GetMapping("/templates")
  public List<ProductionDtos.ProcessTemplateResponse> listTemplates() {
    return productionService.listTemplates();
  }

  @GetMapping("/templates/{id}")
  public ProductionDtos.ProcessTemplateResponse getTemplate(@PathVariable Long id) {
    return productionService.getTemplate(id);
  }

  @PostMapping("/templates")
  public ProductionDtos.ProcessTemplateResponse createTemplate(
      @Valid @RequestBody ProductionDtos.ProcessTemplateRequest request) {
    return productionService.createTemplate(request);
  }

  @PutMapping("/templates/{id}")
  public ProductionDtos.ProcessTemplateResponse updateTemplate(
      @PathVariable Long id,
      @Valid @RequestBody ProductionDtos.ProcessTemplateRequest request) {
    return productionService.updateTemplate(id, request);
  }

  @DeleteMapping("/templates/{id}")
  public void deleteTemplate(@PathVariable Long id) {
    productionService.deleteTemplate(id);
  }

  // Orders (legacy compatibility)
  @GetMapping("/orders")
  public List<ProductionDtos.ProductionOrderResponse> listOrders() {
    return productionService.listOrders();
  }

  @GetMapping("/orders/{id}")
  public ProductionDtos.ProductionOrderResponse getOrder(@PathVariable Long id) {
    return productionService.getOrder(id);
  }

  @PostMapping("/orders")
  public ProductionDtos.ProductionOrderResponse createOrder(
      @Valid @RequestBody ProductionDtos.ProductionOrderRequest request) {
    return productionService.createOrder(request);
  }

  @PutMapping("/orders/{id}")
  public ProductionDtos.ProductionOrderResponse updateOrder(
      @PathVariable Long id,
      @Valid @RequestBody ProductionDtos.ProductionOrderRequest request) {
    return productionService.updateOrder(id, request);
  }

  @DeleteMapping("/orders/{id}")
  public void deleteOrder(@PathVariable Long id) {
    productionService.deleteOrder(id);
  }

  @GetMapping("/batches")
  public List<ProductionDtos.ProductionBatchResponse> listBatches(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Long templateId) {
    return productionService.listBatches(q, status, templateId);
  }

  @PostMapping("/batches")
  public ProductionDtos.ProductionBatchResponse createBatch(
      @Valid @RequestBody ProductionDtos.ProductionBatchRequest request) {
    return productionService.createBatch(request);
  }

  @GetMapping("/batches/{id}")
  public ProductionDtos.ProductionBatchResponse getBatch(@PathVariable Long id) {
    return productionService.getBatch(id);
  }

  @PostMapping("/batches/{id}/start")
  public ProductionDtos.ProductionBatchResponse startBatch(@PathVariable Long id) {
    return productionService.startBatch(id);
  }

  @PostMapping("/batches/{id}/issue")
  public void issueMaterials(@PathVariable Long id, @Valid @RequestBody ProductionDtos.BatchIssueRequest request) {
    productionService.issueMaterials(id, request);
  }

  @PostMapping("/batches/{id}/step/{stepNo}/complete")
  public void completeStep(@PathVariable Long id, @PathVariable Integer stepNo,
                           @RequestBody(required = false) ProductionDtos.BatchStepCompleteRequest request) {
    productionService.completeStep(id, stepNo, request);
  }

  @PostMapping("/batches/{id}/produce")
  public void produce(@PathVariable Long id, @Valid @RequestBody ProductionDtos.BatchProduceRequest request) {
    productionService.produce(id, request);
  }

  @PostMapping("/batches/{id}/complete")
  public ProductionDtos.ProductionBatchResponse completeBatch(@PathVariable Long id) {
    return productionService.completeBatch(id);
  }

  @GetMapping("/batches/{id}/wip-outputs")
  public List<ProductionDtos.WipOutputResponse> listWipOutputs(@PathVariable Long id) {
    return productionService.listAvailableWipOutputs(id);
  }

  @GetMapping("/batches/{id}/cost-summary")
  public ProductionDtos.BatchCostSummaryResponse getCostSummary(@PathVariable Long id) {
    return productionService.getCostSummary(id);
  }

  @GetMapping("/wip/balances")
  public List<ProductionDtos.WipOutputResponse> wipBalances() {
    return productionRunService.listWipBalances();
  }

  // Runs
  @GetMapping("/batches/{id}/runs")
  public List<ProductionDtos.ProductionRunResponse> listRuns(@PathVariable Long id) {
    return productionRunService.listRunsForBatch(id);
  }

  @PostMapping("/batches/{id}/runs")
  public ProductionDtos.ProductionRunResponse createRun(@PathVariable Long id,
                                                        @Valid @RequestBody ProductionDtos.ProductionRunRequest request) {
    return productionRunService.createRun(id, request);
  }

  @GetMapping("/runs/{id}")
  public ProductionDtos.ProductionRunResponse getRun(@PathVariable Long id) {
    return productionRunService.getRun(id);
  }

  @GetMapping("/runs/{id}/cost-summary")
  public ProductionDtos.RunCostSummaryResponse getRunCostSummary(@PathVariable Long id) {
    return productionRunService.getRunCostSummary(id);
  }

  @PutMapping("/runs/{id}")
  public ProductionDtos.ProductionRunResponse updateRun(@PathVariable Long id,
                                                        @Valid @RequestBody ProductionDtos.ProductionRunRequest request) {
    return productionRunService.updateRun(id, request);
  }

  @PostMapping("/runs/{id}/post")
  public ProductionDtos.ProductionRunResponse postRun(@PathVariable Long id) {
    return productionRunService.postRun(id);
  }

  @GetMapping("/batches/{id}/wip")
  public List<ProductionDtos.WipSelectionResponse> listBatchWip(@PathVariable Long id) {
    return productionRunService.listAvailableWipForBatch(id);
  }
}
