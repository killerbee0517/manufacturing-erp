package com.manufacturing.erp.controller;

import com.manufacturing.erp.dto.ProductionDtos;
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

  public ProductionController(ProductionService productionService) {
    this.productionService = productionService;
  }

  // BOM
  @GetMapping("/bom")
  public List<ProductionDtos.BomResponse> listBoms() {
    return productionService.listBoms();
  }

  @GetMapping("/bom/{id}")
  public ProductionDtos.BomResponse getBom(@PathVariable Long id) {
    return productionService.getBom(id);
  }

  @PostMapping("/bom")
  public ProductionDtos.BomResponse createBom(@Valid @RequestBody ProductionDtos.BomRequest request) {
    return productionService.createBom(request);
  }

  @PutMapping("/bom/{id}")
  public ProductionDtos.BomResponse updateBom(@PathVariable Long id, @Valid @RequestBody ProductionDtos.BomRequest request) {
    return productionService.updateBom(id, request);
  }

  @DeleteMapping("/bom/{id}")
  public void deleteBom(@PathVariable Long id) {
    productionService.deleteBom(id);
  }

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
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Long templateId) {
    return productionService.listBatches(status, templateId);
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
    return productionService.listWipBalances();
  }
}
