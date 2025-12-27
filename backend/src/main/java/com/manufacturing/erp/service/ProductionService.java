package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.BomHeader;
import com.manufacturing.erp.domain.BomLine;
import com.manufacturing.erp.domain.Enums.InventoryLocationType;
import com.manufacturing.erp.domain.Enums.LedgerTxnType;
import com.manufacturing.erp.domain.Enums.ProcessInputSourceType;
import com.manufacturing.erp.domain.Enums.ProcessOutputType;
import com.manufacturing.erp.domain.Enums.ProductionStatus;
import com.manufacturing.erp.domain.Enums.StockStatus;
import com.manufacturing.erp.domain.Godown;
import com.manufacturing.erp.domain.InventoryMovement;
import com.manufacturing.erp.domain.Item;
import com.manufacturing.erp.domain.ProcessTemplate;
import com.manufacturing.erp.domain.ProcessTemplateInput;
import com.manufacturing.erp.domain.ProcessTemplateStep;
import com.manufacturing.erp.domain.ProcessTemplateStep.StepType;
import com.manufacturing.erp.domain.ProductionBatch;
import com.manufacturing.erp.domain.ProductionBatchInput;
import com.manufacturing.erp.domain.ProductionBatchOutput;
import com.manufacturing.erp.domain.ProductionBatchStep;
import com.manufacturing.erp.domain.ProductionOrder;
import com.manufacturing.erp.domain.Uom;
import com.manufacturing.erp.dto.ProductionDtos;
import com.manufacturing.erp.repository.BomHeaderRepository;
import com.manufacturing.erp.repository.GodownRepository;
import com.manufacturing.erp.repository.InventoryMovementRepository;
import com.manufacturing.erp.repository.ItemRepository;
import com.manufacturing.erp.repository.ProcessTemplateInputRepository;
import com.manufacturing.erp.repository.ProcessTemplateRepository;
import com.manufacturing.erp.repository.ProcessTemplateStepRepository;
import com.manufacturing.erp.repository.ProductionBatchInputRepository;
import com.manufacturing.erp.repository.ProductionBatchOutputRepository;
import com.manufacturing.erp.repository.ProductionBatchRepository;
import com.manufacturing.erp.repository.ProductionBatchStepRepository;
import com.manufacturing.erp.repository.ProductionOrderRepository;
import com.manufacturing.erp.repository.UomRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductionService {
  private final ProcessTemplateRepository processTemplateRepository;
  private final ProcessTemplateInputRepository processTemplateInputRepository;
  private final ProcessTemplateStepRepository processTemplateStepRepository;
  private final ProductionOrderRepository productionOrderRepository;
  private final ProductionBatchRepository productionBatchRepository;
  private final ProductionBatchInputRepository productionBatchInputRepository;
  private final ProductionBatchOutputRepository productionBatchOutputRepository;
  private final ProductionBatchStepRepository productionBatchStepRepository;
  private final BomHeaderRepository bomHeaderRepository;
  private final GodownRepository godownRepository;
  private final ItemRepository itemRepository;
  private final UomRepository uomRepository;
  private final InventoryMovementRepository inventoryMovementRepository;
  private final StockLedgerService stockLedgerService;

  public ProductionService(ProcessTemplateRepository processTemplateRepository,
                           ProcessTemplateInputRepository processTemplateInputRepository,
                           ProcessTemplateStepRepository processTemplateStepRepository,
                           ProductionOrderRepository productionOrderRepository,
                           ProductionBatchRepository productionBatchRepository,
                           ProductionBatchInputRepository productionBatchInputRepository,
                           ProductionBatchOutputRepository productionBatchOutputRepository,
                           ProductionBatchStepRepository productionBatchStepRepository,
                           BomHeaderRepository bomHeaderRepository,
                           GodownRepository godownRepository,
                           ItemRepository itemRepository,
                           UomRepository uomRepository,
                           InventoryMovementRepository inventoryMovementRepository,
                           StockLedgerService stockLedgerService) {
    this.processTemplateRepository = processTemplateRepository;
    this.processTemplateInputRepository = processTemplateInputRepository;
    this.processTemplateStepRepository = processTemplateStepRepository;
    this.productionOrderRepository = productionOrderRepository;
    this.productionBatchRepository = productionBatchRepository;
    this.productionBatchInputRepository = productionBatchInputRepository;
    this.productionBatchOutputRepository = productionBatchOutputRepository;
    this.productionBatchStepRepository = productionBatchStepRepository;
    this.bomHeaderRepository = bomHeaderRepository;
    this.godownRepository = godownRepository;
    this.itemRepository = itemRepository;
    this.uomRepository = uomRepository;
    this.inventoryMovementRepository = inventoryMovementRepository;
    this.stockLedgerService = stockLedgerService;
  }

  // region BOM
  public List<ProductionDtos.BomResponse> listBoms() {
    return bomHeaderRepository.findAll().stream().map(this::toBomResponse).toList();
  }

  public ProductionDtos.BomResponse getBom(Long id) {
    BomHeader bom = bomHeaderRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("BOM not found"));
    return toBomResponse(bom);
  }

  @Transactional
  public ProductionDtos.BomResponse createBom(ProductionDtos.BomRequest request) {
    BomHeader bom = new BomHeader();
    bom.setFinishedItem(fetchItem(request.finishedItemId()));
    bom.setName(request.name());
    bom.setVersion(request.version());
    bom.setEnabled(request.enabled() != null ? request.enabled() : Boolean.TRUE);
    BomHeader saved = bomHeaderRepository.save(bom);
    List<BomLine> lines = buildBomLines(saved, request.lines());
    saved.getLines().clear();
    saved.getLines().addAll(lines);
    BomHeader updated = bomHeaderRepository.save(saved);
    return toBomResponse(updated);
  }

  @Transactional
  public ProductionDtos.BomResponse updateBom(Long id, ProductionDtos.BomRequest request) {
    BomHeader bom = bomHeaderRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("BOM not found"));
    bom.setFinishedItem(fetchItem(request.finishedItemId()));
    bom.setName(request.name());
    bom.setVersion(request.version());
    bom.setEnabled(request.enabled() != null ? request.enabled() : Boolean.TRUE);
    bom.getLines().clear();
    bom.getLines().addAll(buildBomLines(bom, request.lines()));
    return toBomResponse(bomHeaderRepository.save(bom));
  }

  @Transactional
  public void deleteBom(Long id) {
    bomHeaderRepository.deleteById(id);
  }
  // endregion

  public List<ProductionDtos.ProcessTemplateResponse> listTemplates() {
    return processTemplateRepository.findAll().stream().map(this::toTemplateResponse).toList();
  }

  public ProductionDtos.ProcessTemplateResponse getTemplate(Long id) {
    ProcessTemplate template = processTemplateRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Process template not found"));
    return toTemplateResponse(template);
  }

  @Transactional
  public ProductionDtos.ProcessTemplateResponse createTemplate(ProductionDtos.ProcessTemplateRequest request) {
    ProcessTemplate template = new ProcessTemplate();
    template.setCode(request.code() != null ? request.code() : "PT-" + System.currentTimeMillis());
    template.setName(request.name());
    template.setDescription(request.description());
    template.setOutputItem(request.outputItemId() != null ? fetchItem(request.outputItemId()) : null);
    template.setOutputUom(request.outputUomId() != null ? fetchUom(request.outputUomId()) : null);
    template.setEnabled(request.enabled() != null ? request.enabled() : Boolean.TRUE);
    ProcessTemplate saved = processTemplateRepository.save(template);
    List<ProcessTemplateStep> steps = buildSteps(request.steps(), saved);
    List<ProcessTemplateInput> inputs = buildInputs(request.inputs(), saved);
    saved.getSteps().clear();
    saved.getSteps().addAll(steps);
    saved.getInputs().clear();
    saved.getInputs().addAll(inputs);
    return toTemplateResponse(processTemplateRepository.save(saved));
  }

  @Transactional
  public ProductionDtos.ProcessTemplateResponse updateTemplate(Long id, ProductionDtos.ProcessTemplateRequest request) {
    ProcessTemplate template = processTemplateRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Process template not found"));
    template.setCode(request.code());
    template.setName(request.name());
    template.setDescription(request.description());
    template.setOutputItem(request.outputItemId() != null ? fetchItem(request.outputItemId()) : null);
    template.setOutputUom(request.outputUomId() != null ? fetchUom(request.outputUomId()) : null);
    template.setEnabled(request.enabled() != null ? request.enabled() : template.getEnabled());
    template.getSteps().clear();
    template.getSteps().addAll(buildSteps(request.steps(), template));
    template.getInputs().clear();
    template.getInputs().addAll(buildInputs(request.inputs(), template));
    return toTemplateResponse(processTemplateRepository.save(template));
  }

  @Transactional
  public void deleteTemplate(Long id) {
    processTemplateRepository.deleteById(id);
  }

  public List<ProductionDtos.ProductionOrderResponse> listOrders() {
    return productionOrderRepository.findAll().stream().map(this::toOrderResponse).toList();
  }

  public ProductionDtos.ProductionOrderResponse getOrder(Long id) {
    ProductionOrder order = productionOrderRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Production order not found"));
    return toOrderResponse(order);
  }

  @Transactional
  public ProductionDtos.ProductionOrderResponse createOrder(ProductionDtos.ProductionOrderRequest request) {
    ProductionOrder order = new ProductionOrder();
    order.setOrderNo(request.orderNo() != null ? request.orderNo() : "ORD-" + System.currentTimeMillis());
    order.setTemplate(fetchTemplate(request.templateId()));
    order.setBom(fetchBom(request.bomId()));
    order.setFinishedItem(fetchItem(request.finishedItemId()));
    order.setUom(fetchUom(request.uomId()));
    order.setPlannedQty(request.plannedQty());
    order.setOrderDate(request.orderDate());
    order.setStatus(ProductionStatus.DRAFT);
    return toOrderResponse(productionOrderRepository.save(order));
  }

  @Transactional
  public ProductionDtos.ProductionOrderResponse updateOrder(Long id, ProductionDtos.ProductionOrderRequest request) {
    ProductionOrder order = productionOrderRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Production order not found"));
    order.setOrderNo(request.orderNo());
    order.setTemplate(fetchTemplate(request.templateId()));
    order.setBom(fetchBom(request.bomId()));
    order.setFinishedItem(fetchItem(request.finishedItemId()));
    order.setUom(fetchUom(request.uomId()));
    order.setPlannedQty(request.plannedQty());
    order.setOrderDate(request.orderDate());
    return toOrderResponse(productionOrderRepository.save(order));
  }

  @Transactional
  public void deleteOrder(Long id) {
    productionOrderRepository.deleteById(id);
  }

  // region batches
  @Transactional
  public ProductionDtos.ProductionBatchResponse createBatch(ProductionDtos.ProductionBatchRequest request) {
    ProcessTemplate template = fetchTemplate(request.templateId());
    ProductionBatch batch = new ProductionBatch();
    batch.setBatchNo("BATCH-" + System.currentTimeMillis());
    batch.setTemplate(template);
    batch.setStatus(ProductionStatus.DRAFT);
    batch.setPlannedOutputQty(request.plannedOutputQty());
    batch.setUom(request.uomId() != null ? fetchUom(request.uomId()) : template.getOutputUom());
    batch.setRemarks(request.remarks());
    ProductionBatch saved = productionBatchRepository.save(batch);
    return toBatchResponse(saved);
  }

  @Transactional(readOnly = true)
  public List<ProductionDtos.ProductionBatchResponse> listBatches(String status, Long templateId) {
    return productionBatchRepository.findAll().stream()
        .filter(batch -> status == null || batch.getStatus().name().equalsIgnoreCase(status))
        .filter(batch -> templateId == null || (batch.getTemplate() != null && batch.getTemplate().getId().equals(templateId)))
        .map(this::toBatchResponse)
        .toList();
  }

  @Transactional
  public ProductionDtos.ProductionBatchResponse startBatch(Long batchId) {
    ProductionBatch batch = productionBatchRepository.findById(batchId)
        .orElseThrow(() -> new IllegalArgumentException("Batch not found"));
    batch.setStatus(ProductionStatus.RUNNING);
    batch.setStartedAt(Instant.now());
    if (batch.getStartDate() == null) {
      batch.setStartDate(LocalDate.now());
    }
    productionBatchStepRepository.deleteAll(productionBatchStepRepository.findByBatchIdOrderByStepNoAsc(batchId));
    List<ProductionBatchStep> steps = new ArrayList<>();
    List<ProcessTemplateStep> templateSteps = processTemplateStepRepository.findByTemplateIdOrderByStepNoAsc(batch.getTemplate().getId());
    for (ProcessTemplateStep step : templateSteps) {
      ProductionBatchStep s = new ProductionBatchStep();
      s.setBatch(batch);
      s.setStepNo(step.getStepNo());
      s.setStepName(step.getStepName());
      s.setStatus(ProductionBatchStep.StepStatus.PENDING);
      steps.add(s);
    }
    productionBatchStepRepository.saveAll(steps);
    return toBatchResponse(productionBatchRepository.save(batch));
  }

  @Transactional(readOnly = true)
  public ProductionDtos.ProductionBatchResponse getBatch(Long id) {
    return toBatchResponse(productionBatchRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Batch not found")));
  }

  @Transactional
  public void issueMaterials(Long batchId, ProductionDtos.BatchIssueRequest request) {
    ProductionBatch batch = productionBatchRepository.findById(batchId)
        .orElseThrow(() -> new IllegalArgumentException("Batch not found"));
    List<ProductionBatchInput> inputs = new ArrayList<>();
    for (ProductionDtos.BatchInputRequest line : request.inputs()) {
      ProductionBatchInput input = new ProductionBatchInput();
      input.setBatch(batch);
      input.setItem(fetchItem(line.itemId()));
      input.setUom(fetchUom(line.uomId()));
      input.setIssuedQty(line.qty());
      input.setSourceType(ProcessInputSourceType.valueOf(line.sourceType()));
      input.setSourceRefId(line.sourceRefId());
      input.setSourceGodown(line.sourceGodownId() != null ? fetchGodown(line.sourceGodownId()) : null);
      input.setIssuedAt(line.issuedAt() != null ? line.issuedAt() : Instant.now());
      inputs.add(input);

      // update WIP consumption if needed
      if (input.getSourceType() == ProcessInputSourceType.WIP && line.sourceRefId() != null) {
        ProductionBatchOutput source = productionBatchOutputRepository.findById(line.sourceRefId())
            .orElseThrow(() -> new IllegalArgumentException("WIP source not found"));
        if (source.getOutputType() != ProcessOutputType.WIP) {
          throw new IllegalArgumentException("Only WIP outputs can be consumed as WIP inputs");
        }
        BigDecimal available = source.getProducedQty().subtract(source.getConsumedQty());
        if (available.compareTo(line.qty()) < 0) {
          throw new IllegalArgumentException("Insufficient WIP quantity");
        }
        source.setConsumedQty(source.getConsumedQty().add(line.qty()));
        productionBatchOutputRepository.save(source);
        recordMovement("PROD_ISSUE", batch, input.getItem(), input.getUom(), BigDecimal.ZERO, line.qty(),
            InventoryLocationType.WIP, source.getId());
      } else {
        recordMovement("PROD_ISSUE", batch, input.getItem(), input.getUom(), BigDecimal.ZERO, line.qty(),
            InventoryLocationType.GODOWN, input.getSourceGodown() != null ? input.getSourceGodown().getId() : null);
        if (input.getSourceGodown() != null) {
          stockLedgerService.postEntry("PROD_ISSUE", batch.getId(), null, LedgerTxnType.OUT,
              input.getItem(), input.getUom(), null, null, input.getSourceGodown(), null, null, null,
              line.qty(), line.qty(), StockStatus.UNRESTRICTED, null, null);
        }
      }
    }
    productionBatchInputRepository.saveAll(inputs);
  }

  @Transactional
  public void completeStep(Long batchId, Integer stepNo, ProductionDtos.BatchStepCompleteRequest request) {
    List<ProductionBatchStep> steps = productionBatchStepRepository.findByBatchIdOrderByStepNoAsc(batchId);
    ProductionBatchStep step = steps.stream()
        .filter(s -> s.getStepNo().equals(stepNo))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Step not found"));
    step.setStatus(ProductionBatchStep.StepStatus.DONE);
    step.setStartedAt(step.getStartedAt() != null ? step.getStartedAt() : Instant.now());
    step.setCompletedAt(Instant.now());
    step.setNotes(request != null ? request.notes() : null);
    productionBatchStepRepository.save(step);
  }

  @Transactional
  public void produce(Long batchId, ProductionDtos.BatchProduceRequest request) {
    ProductionBatch batch = productionBatchRepository.findById(batchId)
        .orElseThrow(() -> new IllegalArgumentException("Batch not found"));
    List<ProductionBatchOutput> outputs = new ArrayList<>();
    for (ProductionDtos.BatchOutputRequest line : request.outputs()) {
      ProductionBatchOutput output = new ProductionBatchOutput();
      output.setBatch(batch);
      output.setItem(fetchItem(line.itemId()));
      output.setUom(fetchUom(line.uomId()));
      output.setProducedQty(line.qty());
      output.setOutputType(ProcessOutputType.valueOf(line.outputType()));
      output.setDestinationGodown(line.destinationGodownId() != null ? fetchGodown(line.destinationGodownId()) : null);
      output.setProducedAt(line.producedAt() != null ? line.producedAt() : Instant.now());
      outputs.add(output);

      InventoryLocationType locationType = output.getOutputType() == ProcessOutputType.FG
          ? InventoryLocationType.GODOWN
          : InventoryLocationType.WIP;
      Long locationId = output.getDestinationGodown() != null ? output.getDestinationGodown().getId() : output.getBatch().getId();
      recordMovement("PROD_OUTPUT", batch, output.getItem(), output.getUom(), line.qty(), BigDecimal.ZERO, locationType, locationId);
      if (output.getOutputType() == ProcessOutputType.FG && output.getDestinationGodown() != null) {
        stockLedgerService.postEntry("PROD_OUTPUT", batch.getId(), null, LedgerTxnType.IN,
            output.getItem(), output.getUom(), null, null, null, output.getDestinationGodown(), null, null,
            line.qty(), line.qty(), StockStatus.UNRESTRICTED, null, null);
      }
    }
    productionBatchOutputRepository.saveAll(outputs);
  }

  @Transactional
  public ProductionDtos.ProductionBatchResponse completeBatch(Long batchId) {
    ProductionBatch batch = productionBatchRepository.findById(batchId)
        .orElseThrow(() -> new IllegalArgumentException("Batch not found"));
    List<ProductionBatchOutput> outputs = productionBatchOutputRepository.findByBatchId(batchId);
    BigDecimal totalProduced = outputs.stream().map(ProductionBatchOutput::getProducedQty).reduce(BigDecimal.ZERO, BigDecimal::add);
    if (totalProduced.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalStateException("Produce something before completing batch");
    }
    batch.setStatus(ProductionStatus.COMPLETED);
    batch.setCompletedAt(Instant.now());
    batch.setEndDate(LocalDate.now());
    return toBatchResponse(productionBatchRepository.save(batch));
  }

  @Transactional(readOnly = true)
  public List<ProductionDtos.WipOutputResponse> listAvailableWipOutputs(Long batchId) {
    return productionBatchOutputRepository.findByBatchId(batchId).stream()
        .filter(out -> out.getOutputType() == ProcessOutputType.WIP)
        .map(out -> {
          BigDecimal available = out.getProducedQty().subtract(out.getConsumedQty());
          return new ProductionDtos.WipOutputResponse(
              out.getId(),
              out.getBatch().getId(),
              out.getItem().getId(),
              out.getItem().getName(),
              out.getUom().getId(),
              out.getUom().getCode(),
              out.getProducedQty(),
              out.getConsumedQty(),
              available
          );
        })
        .filter(res -> res.availableQuantity().compareTo(BigDecimal.ZERO) > 0)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ProductionDtos.WipOutputResponse> listWipBalances() {
    return productionBatchOutputRepository.findAll().stream()
        .filter(out -> out.getOutputType() == ProcessOutputType.WIP)
        .map(out -> {
          BigDecimal available = out.getProducedQty().subtract(out.getConsumedQty());
          return new ProductionDtos.WipOutputResponse(
              out.getId(),
              out.getBatch().getId(),
              out.getItem().getId(),
              out.getItem().getName(),
              out.getUom().getId(),
              out.getUom().getCode(),
              out.getProducedQty(),
              out.getConsumedQty(),
              available
          );
        })
        .filter(res -> res.availableQuantity().compareTo(BigDecimal.ZERO) > 0)
        .toList();
  }

  @Transactional(readOnly = true)
  public ProductionDtos.BatchCostSummaryResponse getCostSummary(Long batchId) {
    List<ProductionBatchInput> inputs = productionBatchInputRepository.findByBatchId(batchId);
    List<ProductionBatchOutput> outputs = productionBatchOutputRepository.findByBatchId(batchId);
    BigDecimal totalConsumption = inputs.stream().map(ProductionBatchInput::getIssuedQty).reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal totalOutput = outputs.stream().map(ProductionBatchOutput::getProducedQty).reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal unitCost = BigDecimal.ZERO;
    if (totalOutput.compareTo(BigDecimal.ZERO) > 0) {
      unitCost = totalConsumption.divide(totalOutput, 4, RoundingMode.HALF_UP);
    }
    return new ProductionDtos.BatchCostSummaryResponse(batchId, totalConsumption, BigDecimal.ZERO, totalOutput, BigDecimal.ZERO, unitCost);
  }
  // endregion

  private ProcessTemplate fetchTemplate(Long id) {
    if (id == null) {
      return null;
    }
    return processTemplateRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Process template not found"));
  }

  private Item fetchItem(Long id) {
    return itemRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Item not found"));
  }

  private Uom fetchUom(Long id) {
    return uomRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("UOM not found"));
  }

  private Godown fetchGodown(Long id) {
    if (id == null) {
      return null;
    }
    return godownRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Godown not found"));
  }

  private List<ProcessTemplateStep> buildSteps(List<ProductionDtos.ProcessTemplateStepRequest> requests, ProcessTemplate template) {
    if (requests == null) {
      return List.of();
    }
    List<ProcessTemplateStep> steps = new ArrayList<>();
    for (ProductionDtos.ProcessTemplateStepRequest request : requests) {
      ProcessTemplateStep step = new ProcessTemplateStep();
      step.setTemplate(template);
      step.setStepNo(request.stepNo());
      step.setStepName(request.stepName());
      if (request.stepType() != null) {
        step.setStepType(StepType.valueOf(request.stepType()));
      }
      step.setNotes(request.notes());
      steps.add(step);
    }
    steps.sort(Comparator.comparing(ProcessTemplateStep::getStepNo));
    return steps;
  }

  private List<ProcessTemplateInput> buildInputs(List<ProductionDtos.ProcessTemplateInputRequest> requests, ProcessTemplate template) {
    if (requests == null) {
      return List.of();
    }
    List<ProcessTemplateInput> inputs = new ArrayList<>();
    for (ProductionDtos.ProcessTemplateInputRequest request : requests) {
      ProcessTemplateInput input = new ProcessTemplateInput();
      input.setTemplate(template);
      input.setItem(fetchItem(request.itemId()));
      input.setUom(fetchUom(request.uomId()));
      input.setDefaultQty(request.defaultQty());
      input.setOptional(request.optional() != null ? request.optional() : Boolean.FALSE);
      input.setNotes(request.notes());
      inputs.add(input);
    }
    return inputs;
  }

  private List<BomLine> buildBomLines(BomHeader bom, List<ProductionDtos.BomLineRequest> requests) {
    if (requests == null) {
      return Collections.emptyList();
    }
    List<BomLine> lines = new ArrayList<>();
    for (ProductionDtos.BomLineRequest req : requests) {
      BomLine line = new BomLine();
      line.setBom(bom);
      line.setComponentItem(fetchItem(req.componentItemId()));
      line.setUom(fetchUom(req.uomId()));
      line.setQtyPerUnit(req.qtyPerUnit());
      line.setScrapPercent(req.scrapPercent());
      lines.add(line);
    }
    return lines;
  }

  private BomHeader fetchBom(Long id) {
    return bomHeaderRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("BOM not found"));
  }

  private ProductionDtos.ProcessTemplateResponse toTemplateResponse(ProcessTemplate template) {
    List<ProductionDtos.ProcessTemplateStepResponse> steps = processTemplateStepRepository
        .findByTemplateIdOrderByStepNoAsc(template.getId()).stream()
        .map(step -> new ProductionDtos.ProcessTemplateStepResponse(
            step.getId(),
            step.getStepNo(),
            step.getStepName(),
            step.getStepType().name(),
            step.getNotes()
        )).toList();
    List<ProductionDtos.ProcessTemplateInputResponse> inputs = processTemplateInputRepository.findByTemplateId(template.getId()).stream()
        .map(input -> new ProductionDtos.ProcessTemplateInputResponse(
            input.getId(),
            input.getItem().getId(),
            input.getItem().getName(),
            input.getUom().getId(),
            input.getUom().getCode(),
            input.getDefaultQty(),
            input.getOptional(),
            input.getNotes()
        )).toList();
    return new ProductionDtos.ProcessTemplateResponse(
        template.getId(),
        template.getCode(),
        template.getName(),
        template.getDescription(),
        template.getOutputItem() != null ? template.getOutputItem().getId() : null,
        template.getOutputItem() != null ? template.getOutputItem().getName() : null,
        template.getOutputUom() != null ? template.getOutputUom().getId() : null,
        template.getOutputUom() != null ? template.getOutputUom().getCode() : null,
        template.getEnabled(),
        inputs,
        steps
    );
  }

  private ProductionDtos.ProductionOrderResponse toOrderResponse(ProductionOrder order) {
    return new ProductionDtos.ProductionOrderResponse(
        order.getId(),
        order.getOrderNo(),
        order.getTemplate() != null ? order.getTemplate().getId() : null,
        order.getTemplate() != null ? order.getTemplate().getName() : null,
        order.getBom() != null ? order.getBom().getId() : null,
        order.getBom() != null ? order.getBom().getName() : null,
        order.getFinishedItem() != null ? order.getFinishedItem().getId() : null,
        order.getFinishedItem() != null ? order.getFinishedItem().getName() : null,
        order.getUom() != null ? order.getUom().getId() : null,
        order.getUom() != null ? order.getUom().getCode() : null,
        order.getPlannedQty(),
        order.getOrderDate(),
        order.getStatus().name()
    );
  }

  private ProductionDtos.ProductionBatchResponse toBatchResponse(ProductionBatch batch) {
    List<ProductionBatchInput> inputs = productionBatchInputRepository.findByBatchId(batch.getId());
    List<ProductionBatchOutput> outputs = productionBatchOutputRepository.findByBatchId(batch.getId());
    List<ProductionBatchStep> steps = productionBatchStepRepository.findByBatchIdOrderByStepNoAsc(batch.getId());
    return new ProductionDtos.ProductionBatchResponse(
        batch.getId(),
        batch.getBatchNo(),
        batch.getTemplate() != null ? batch.getTemplate().getId() : null,
        batch.getTemplate() != null ? batch.getTemplate().getName() : null,
        batch.getProductionOrder() != null ? batch.getProductionOrder().getId() : null,
        batch.getStatus().name(),
        batch.getPlannedOutputQty(),
        batch.getUom() != null ? batch.getUom().getId() : null,
        batch.getUom() != null ? batch.getUom().getCode() : null,
        batch.getStartDate(),
        batch.getEndDate(),
        batch.getStartedAt(),
        batch.getCompletedAt(),
        inputs.stream().map(input -> new ProductionDtos.BatchInputResponse(
            input.getId(),
            input.getItem().getId(),
            input.getItem().getName(),
            input.getUom().getId(),
            input.getUom().getCode(),
            input.getIssuedQty(),
            input.getSourceType().name(),
            input.getSourceRefId(),
            input.getSourceGodown() != null ? input.getSourceGodown().getId() : null,
            input.getSourceGodown() != null ? input.getSourceGodown().getName() : null,
            input.getIssuedAt()
        )).toList(),
        outputs.stream().map(output -> new ProductionDtos.BatchOutputResponse(
            output.getId(),
            output.getItem().getId(),
            output.getItem().getName(),
            output.getUom().getId(),
            output.getUom().getCode(),
            output.getProducedQty(),
            output.getConsumedQty(),
            output.getOutputType().name(),
            output.getDestinationGodown() != null ? output.getDestinationGodown().getId() : null,
            output.getDestinationGodown() != null ? output.getDestinationGodown().getName() : null,
            output.getProducedAt()
        )).toList(),
        steps.stream().map(step -> new ProductionDtos.BatchStepResponse(
            step.getId(),
            step.getStepNo(),
            step.getStepName(),
            step.getStatus().name(),
            step.getStartedAt(),
            step.getCompletedAt(),
            step.getNotes()
        )).toList()
    );
  }

  private void recordMovement(String txnType, ProductionBatch batch, Item item, Uom uom,
                              BigDecimal qtyIn, BigDecimal qtyOut,
                              InventoryLocationType locationType, Long locationId) {
    InventoryMovement movement = new InventoryMovement();
    movement.setTxnType(txnType);
    movement.setRefType("BATCH");
    movement.setRefId(batch.getId());
    movement.setItem(item);
    movement.setQtyIn(defaultZero(qtyIn));
    movement.setQtyOut(defaultZero(qtyOut));
    movement.setUom(uom);
    movement.setLocationType(locationType);
    movement.setLocationId(locationId);
    inventoryMovementRepository.save(movement);
  }

  private ProductionDtos.BomResponse toBomResponse(BomHeader bom) {
    List<ProductionDtos.BomLineResponse> lineResponses = bom.getLines().stream().map(line -> new ProductionDtos.BomLineResponse(
        line.getId(),
        line.getComponentItem().getId(),
        line.getComponentItem().getName(),
        line.getUom().getId(),
        line.getUom().getCode(),
        line.getQtyPerUnit(),
        line.getScrapPercent()
    )).toList();
    return new ProductionDtos.BomResponse(
        bom.getId(),
        bom.getFinishedItem().getId(),
        bom.getFinishedItem().getName(),
        bom.getName(),
        bom.getVersion(),
        bom.getEnabled(),
        lineResponses
    );
  }

  private BigDecimal defaultZero(BigDecimal value) {
    return value != null ? value : BigDecimal.ZERO;
  }
}
