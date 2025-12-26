package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.BomHeader;
import com.manufacturing.erp.domain.BomLine;
import com.manufacturing.erp.domain.Enums.LedgerTxnType;
import com.manufacturing.erp.domain.Enums.ProcessInputSourceType;
import com.manufacturing.erp.domain.Enums.ProcessOutputType;
import com.manufacturing.erp.domain.Enums.ProductionStatus;
import com.manufacturing.erp.domain.Enums.StockStatus;
import com.manufacturing.erp.domain.Godown;
import com.manufacturing.erp.domain.Item;
import com.manufacturing.erp.domain.ProcessRun;
import com.manufacturing.erp.domain.ProcessRunConsumption;
import com.manufacturing.erp.domain.ProcessRunOutput;
import com.manufacturing.erp.domain.ProcessStep;
import com.manufacturing.erp.domain.ProcessTemplate;
import com.manufacturing.erp.domain.ProductionBatch;
import com.manufacturing.erp.domain.ProductionOrder;
import com.manufacturing.erp.domain.Uom;
import com.manufacturing.erp.dto.ProductionDtos;
import com.manufacturing.erp.repository.BomHeaderRepository;
import com.manufacturing.erp.repository.GodownRepository;
import com.manufacturing.erp.repository.ItemRepository;
import com.manufacturing.erp.repository.ProcessRunConsumptionRepository;
import com.manufacturing.erp.repository.ProcessRunOutputRepository;
import com.manufacturing.erp.repository.ProcessRunRepository;
import com.manufacturing.erp.repository.ProcessStepRepository;
import com.manufacturing.erp.repository.ProcessTemplateRepository;
import com.manufacturing.erp.repository.ProductionBatchRepository;
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
  private final ProcessStepRepository processStepRepository;
  private final ProductionOrderRepository productionOrderRepository;
  private final ProductionBatchRepository productionBatchRepository;
  private final ProcessRunRepository processRunRepository;
  private final ProcessRunConsumptionRepository processRunConsumptionRepository;
  private final ProcessRunOutputRepository processRunOutputRepository;
  private final BomHeaderRepository bomHeaderRepository;
  private final GodownRepository godownRepository;
  private final ItemRepository itemRepository;
  private final UomRepository uomRepository;
  private final StockLedgerService stockLedgerService;

  public ProductionService(ProcessTemplateRepository processTemplateRepository,
                           ProcessStepRepository processStepRepository,
                           ProductionOrderRepository productionOrderRepository,
                           ProductionBatchRepository productionBatchRepository,
                           ProcessRunRepository processRunRepository,
                           ProcessRunConsumptionRepository processRunConsumptionRepository,
                           ProcessRunOutputRepository processRunOutputRepository,
                           BomHeaderRepository bomHeaderRepository,
                           GodownRepository godownRepository,
                           ItemRepository itemRepository,
                           UomRepository uomRepository,
                           StockLedgerService stockLedgerService) {
    this.processTemplateRepository = processTemplateRepository;
    this.processStepRepository = processStepRepository;
    this.productionOrderRepository = productionOrderRepository;
    this.productionBatchRepository = productionBatchRepository;
    this.processRunRepository = processRunRepository;
    this.processRunConsumptionRepository = processRunConsumptionRepository;
    this.processRunOutputRepository = processRunOutputRepository;
    this.bomHeaderRepository = bomHeaderRepository;
    this.godownRepository = godownRepository;
    this.itemRepository = itemRepository;
    this.uomRepository = uomRepository;
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
    template.setName(request.name());
    template.setDescription(request.description());
    ProcessTemplate saved = processTemplateRepository.save(template);
    List<ProcessStep> steps = buildSteps(request.steps(), saved);
    saved.getSteps().clear();
    saved.getSteps().addAll(steps);
    ProcessTemplate updated = processTemplateRepository.save(saved);
    return toTemplateResponse(updated);
  }

  @Transactional
  public ProductionDtos.ProcessTemplateResponse updateTemplate(Long id, ProductionDtos.ProcessTemplateRequest request) {
    ProcessTemplate template = processTemplateRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Process template not found"));
    template.setName(request.name());
    template.setDescription(request.description());
    template.getSteps().clear();
    template.getSteps().addAll(buildSteps(request.steps(), template));
    ProcessTemplate saved = processTemplateRepository.save(template);
    return toTemplateResponse(saved);
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

  @Transactional
  public ProductionDtos.ProductionBatchResponse startBatch(Long orderId) {
    ProductionOrder order = productionOrderRepository.findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("Production order not found"));
    ProductionBatch batch = new ProductionBatch();
    batch.setBatchNo("BATCH-" + System.currentTimeMillis());
    batch.setProductionOrder(order);
    batch.setStatus(ProductionStatus.STARTED);
    batch.setStartDate(order.getOrderDate());
    batch.setStartedAt(Instant.now());
    ProductionBatch saved = productionBatchRepository.save(batch);
    if (order.getStatus() == ProductionStatus.DRAFT) {
      order.setStatus(ProductionStatus.STARTED);
      productionOrderRepository.save(order);
    }
    return toBatchResponse(saved);
  }

  public List<ProductionDtos.ProductionBatchResponse> listBatches(Long orderId) {
    return productionBatchRepository.findByProductionOrderId(orderId).stream()
        .map(this::toBatchResponse)
        .toList();
  }

  @Transactional
  public ProductionDtos.ProcessRunResponse createRun(ProductionDtos.ProcessRunRequest request) {
    ProductionBatch batch = productionBatchRepository.findById(request.batchId())
        .orElseThrow(() -> new IllegalArgumentException("Production batch not found"));
    ProcessStep step = request.stepId() != null
        ? processStepRepository.findById(request.stepId())
            .orElseThrow(() -> new IllegalArgumentException("Process step not found"))
        : null;

    ProcessRun run = new ProcessRun();
    run.setProductionBatch(batch);
    run.setProcessStep(step);
    run.setStepName(request.stepName() != null ? request.stepName() : step != null ? step.getName() : "Adhoc");
    run.setRunDate(request.runDate() != null ? request.runDate() : LocalDate.now());
    run.setStartedAt(request.startedAt());
    run.setEndedAt(request.endedAt());
    run.setStatus(ProductionStatus.COMPLETED);
    ProcessRun savedRun = processRunRepository.save(run);

    BigDecimal totalConsumptionAmount = BigDecimal.ZERO;
    List<ProcessRunConsumption> consumptions = new ArrayList<>();
    if (request.consumptions() != null) {
      for (ProductionDtos.ProcessRunInputRequest itemRequest : request.consumptions()) {
        Item item = fetchItem(itemRequest.itemId());
        Uom uom = fetchUom(itemRequest.uomId());
        ProcessInputSourceType sourceType = ProcessInputSourceType.valueOf(itemRequest.sourceType());
        ProcessRunOutput sourceOutput = null;
        Godown sourceGodown = null;
        if (sourceType == ProcessInputSourceType.WIP) {
          sourceOutput = processRunOutputRepository.findById(itemRequest.sourceRunOutputId())
              .orElseThrow(() -> new IllegalArgumentException("WIP output not found"));
          if (!sourceOutput.getProcessRun().getProductionBatch().getId().equals(batch.getId())) {
            throw new IllegalArgumentException("WIP output must belong to same batch");
          }
          BigDecimal available = sourceOutput.getQuantity().subtract(sourceOutput.getConsumedQuantity());
          if (available.compareTo(itemRequest.quantity()) < 0) {
            throw new IllegalArgumentException("Insufficient WIP quantity available");
          }
          sourceOutput.setConsumedQuantity(sourceOutput.getConsumedQuantity().add(itemRequest.quantity()));
          processRunOutputRepository.save(sourceOutput);
        } else {
          sourceGodown = fetchGodown(itemRequest.sourceGodownId());
          stockLedgerService.postEntry("PROCESS_RUN", savedRun.getId(), null, LedgerTxnType.OUT,
              item, uom, null, null, sourceGodown, null, null, null, itemRequest.quantity(), itemRequest.quantity(), StockStatus.UNRESTRICTED,
              itemRequest.rate(), itemRequest.amount());
        }
        ProcessRunConsumption consumption = new ProcessRunConsumption();
        consumption.setProcessRun(savedRun);
        consumption.setItem(item);
        consumption.setUom(uom);
        consumption.setSourceType(sourceType);
        consumption.setSourceGodown(sourceGodown);
        consumption.setSourceRunOutput(sourceOutput);
        consumption.setQuantity(itemRequest.quantity());
        consumption.setRate(itemRequest.rate());
        consumption.setAmount(itemRequest.amount());
        consumptions.add(consumption);
        totalConsumptionAmount = totalConsumptionAmount.add(defaultZero(itemRequest.amount()));
      }
    }
    processRunConsumptionRepository.saveAll(consumptions);

    List<ProcessRunOutput> outputs = new ArrayList<>();
    if (request.outputs() != null) {
      for (ProductionDtos.ProcessRunOutputRequest itemRequest : request.outputs()) {
        Item item = fetchItem(itemRequest.itemId());
        Uom uom = fetchUom(itemRequest.uomId());
        ProcessOutputType outputType = ProcessOutputType.valueOf(itemRequest.outputType());
        Godown destGodown = fetchGodown(itemRequest.destGodownId());
        ProcessRunOutput output = new ProcessRunOutput();
        output.setProcessRun(savedRun);
        output.setItem(item);
        output.setUom(uom);
        output.setDestGodown(destGodown);
        output.setQuantity(itemRequest.quantity());
        output.setOutputType(outputType);
        output.setRate(itemRequest.rate());
        output.setAmount(itemRequest.amount());
        outputs.add(output);
        if (outputType == ProcessOutputType.FG) {
          stockLedgerService.postEntry("PROCESS_RUN", savedRun.getId(), null, LedgerTxnType.IN,
              item, uom, null, null, null, destGodown, null, null, itemRequest.quantity(), itemRequest.quantity(), StockStatus.UNRESTRICTED,
              itemRequest.rate(), itemRequest.amount());
        }
      }
    }
    processRunOutputRepository.saveAll(outputs);

    return toRunResponse(savedRun);
  }

  public ProductionDtos.ProcessRunResponse getRun(Long id) {
    ProcessRun run = processRunRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Process run not found"));
    return toRunResponse(run);
  }

  public List<ProductionDtos.ProcessRunResponse> listRuns(Long batchId) {
    return processRunRepository.findByProductionBatchId(batchId).stream().map(this::toRunResponse).toList();
  }

  public List<ProductionDtos.WipOutputResponse> listAvailableWipOutputs(Long batchId) {
    List<ProcessRunOutput> outputs = processRunOutputRepository.findByProcessRunProductionBatchId(batchId);
    List<ProductionDtos.WipOutputResponse> result = new ArrayList<>();
    for (ProcessRunOutput output : outputs) {
      if (output.getOutputType() != ProcessOutputType.WIP) {
        continue;
      }
      BigDecimal available = output.getQuantity().subtract(output.getConsumedQuantity());
      if (available.compareTo(BigDecimal.ZERO) <= 0) {
        continue;
      }
      result.add(new ProductionDtos.WipOutputResponse(
          output.getId(),
          output.getProcessRun().getId(),
          output.getItem().getId(),
          output.getItem().getName(),
          output.getUom().getId(),
          output.getUom().getCode(),
          output.getQuantity(),
          output.getConsumedQuantity(),
          available,
          output.getRate()
      ));
    }
    return result;
  }

  public ProductionDtos.BatchCostSummaryResponse getCostSummary(Long batchId) {
    List<ProcessRun> runs = processRunRepository.findByProductionBatchId(batchId);
    BigDecimal totalConsumption = BigDecimal.ZERO;
    BigDecimal totalConsumptionAmount = BigDecimal.ZERO;
    BigDecimal totalOutput = BigDecimal.ZERO;
    BigDecimal totalOutputAmount = BigDecimal.ZERO;
    for (ProcessRun run : runs) {
      List<ProcessRunConsumption> consumptions = processRunConsumptionRepository.findByProcessRunId(run.getId());
      for (ProcessRunConsumption consumption : consumptions) {
        totalConsumption = totalConsumption.add(consumption.getQuantity());
        totalConsumptionAmount = totalConsumptionAmount.add(defaultZero(consumption.getAmount()));
      }
      List<ProcessRunOutput> outputs = processRunOutputRepository.findByProcessRunId(run.getId());
      for (ProcessRunOutput output : outputs) {
        totalOutput = totalOutput.add(output.getQuantity());
        totalOutputAmount = totalOutputAmount.add(defaultZero(output.getAmount()));
      }
    }
    BigDecimal unitCost = BigDecimal.ZERO;
    if (totalOutput.compareTo(BigDecimal.ZERO) > 0) {
      unitCost = totalConsumptionAmount.compareTo(BigDecimal.ZERO) > 0
          ? totalConsumptionAmount.divide(totalOutput, 4, RoundingMode.HALF_UP)
          : totalConsumption.divide(totalOutput, 4, RoundingMode.HALF_UP);
    }
    return new ProductionDtos.BatchCostSummaryResponse(batchId, totalConsumption, totalConsumptionAmount,
        totalOutput, totalOutputAmount, unitCost);
  }

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

  private List<ProcessStep> buildSteps(List<ProductionDtos.ProcessStepRequest> requests, ProcessTemplate template) {
    if (requests == null) {
      return List.of();
    }
    List<ProcessStep> steps = new ArrayList<>();
    for (ProductionDtos.ProcessStepRequest request : requests) {
      ProcessStep step = new ProcessStep();
      step.setTemplate(template);
      step.setName(request.name());
      step.setDescription(request.description());
      step.setSequenceNo(request.sequenceNo());
      step.setSourceGodown(fetchGodown(request.sourceGodownId()));
      step.setDestGodown(fetchGodown(request.destGodownId()));
      steps.add(step);
    }
    steps.sort(Comparator.comparing(ProcessStep::getSequenceNo));
    return steps;
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
    List<ProductionDtos.ProcessStepResponse> steps = template.getSteps().stream()
        .sorted(Comparator.comparing(ProcessStep::getSequenceNo))
        .map(step -> new ProductionDtos.ProcessStepResponse(
            step.getId(),
            step.getName(),
            step.getDescription(),
            step.getSequenceNo(),
            step.getSourceGodown() != null ? step.getSourceGodown().getId() : null,
            step.getSourceGodown() != null ? step.getSourceGodown().getName() : null,
            step.getDestGodown() != null ? step.getDestGodown().getId() : null,
            step.getDestGodown() != null ? step.getDestGodown().getName() : null
        ))
        .toList();
    return new ProductionDtos.ProcessTemplateResponse(
        template.getId(),
        template.getName(),
        template.getDescription(),
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
    return new ProductionDtos.ProductionBatchResponse(
        batch.getId(),
        batch.getBatchNo(),
        batch.getProductionOrder().getId(),
        batch.getStatus().name(),
        batch.getStartDate(),
        batch.getEndDate(),
        batch.getStartedAt(),
        batch.getCompletedAt()
    );
  }

  private ProductionDtos.ProcessRunResponse toRunResponse(ProcessRun run) {
    return new ProductionDtos.ProcessRunResponse(
        run.getId(),
        run.getProductionBatch().getId(),
        run.getProcessStep() != null ? run.getProcessStep().getId() : null,
        run.getStepName(),
        run.getRunDate(),
        run.getStartedAt(),
        run.getEndedAt(),
        run.getStatus().name()
    );
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
