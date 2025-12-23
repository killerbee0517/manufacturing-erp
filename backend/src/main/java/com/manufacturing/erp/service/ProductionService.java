package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Enums.LedgerTxnType;
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
import java.time.Instant;
import java.util.ArrayList;
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
    this.godownRepository = godownRepository;
    this.itemRepository = itemRepository;
    this.uomRepository = uomRepository;
    this.stockLedgerService = stockLedgerService;
  }

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
    order.setOrderNo(request.orderNo());
    order.setTemplate(fetchTemplate(request.templateId()));
    order.setItem(fetchItem(request.itemId()));
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
    order.setItem(fetchItem(request.itemId()));
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
    ProcessStep step = processStepRepository.findById(request.stepId())
        .orElseThrow(() -> new IllegalArgumentException("Process step not found"));

    ProcessRun run = new ProcessRun();
    run.setProductionBatch(batch);
    run.setProcessStep(step);
    run.setRunDate(request.runDate());
    run.setStatus(ProductionStatus.COMPLETED);
    ProcessRun savedRun = processRunRepository.save(run);

    List<ProcessRunConsumption> consumptions = new ArrayList<>();
    if (request.consumptions() != null) {
      for (ProductionDtos.ProcessRunItemRequest itemRequest : request.consumptions()) {
        Item item = fetchItem(itemRequest.itemId());
        Uom uom = fetchUom(itemRequest.uomId());
        Godown godown = fetchGodown(itemRequest.godownId());
        ProcessRunConsumption consumption = new ProcessRunConsumption();
        consumption.setProcessRun(savedRun);
        consumption.setItem(item);
        consumption.setUom(uom);
        consumption.setGodown(godown);
        consumption.setQuantity(itemRequest.quantity());
        consumptions.add(consumption);
        stockLedgerService.postEntry("PROCESS_RUN", savedRun.getId(), null, LedgerTxnType.OUT,
            item, uom, null, null, godown, null, itemRequest.quantity(), itemRequest.quantity(), StockStatus.UNRESTRICTED);
      }
    }
    processRunConsumptionRepository.saveAll(consumptions);

    List<ProcessRunOutput> outputs = new ArrayList<>();
    if (request.outputs() != null) {
      for (ProductionDtos.ProcessRunItemRequest itemRequest : request.outputs()) {
        Item item = fetchItem(itemRequest.itemId());
        Uom uom = fetchUom(itemRequest.uomId());
        Godown godown = fetchGodown(itemRequest.godownId());
        ProcessRunOutput output = new ProcessRunOutput();
        output.setProcessRun(savedRun);
        output.setItem(item);
        output.setUom(uom);
        output.setGodown(godown);
        output.setQuantity(itemRequest.quantity());
        outputs.add(output);
        stockLedgerService.postEntry("PROCESS_RUN", savedRun.getId(), null, LedgerTxnType.IN,
            item, uom, null, null, null, godown, itemRequest.quantity(), itemRequest.quantity(), StockStatus.UNRESTRICTED);
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

  public ProductionDtos.BatchCostSummaryResponse getCostSummary(Long batchId) {
    List<ProcessRun> runs = processRunRepository.findByProductionBatchId(batchId);
    BigDecimal totalConsumption = BigDecimal.ZERO;
    BigDecimal totalOutput = BigDecimal.ZERO;
    for (ProcessRun run : runs) {
      List<ProcessRunConsumption> consumptions = processRunConsumptionRepository.findByProcessRunId(run.getId());
      for (ProcessRunConsumption consumption : consumptions) {
        totalConsumption = totalConsumption.add(consumption.getQuantity());
      }
      List<ProcessRunOutput> outputs = processRunOutputRepository.findByProcessRunId(run.getId());
      for (ProcessRunOutput output : outputs) {
        totalOutput = totalOutput.add(output.getQuantity());
      }
    }
    BigDecimal unitCost = BigDecimal.ZERO;
    if (totalOutput.compareTo(BigDecimal.ZERO) > 0) {
      unitCost = totalConsumption.divide(totalOutput, 4, java.math.RoundingMode.HALF_UP);
    }
    return new ProductionDtos.BatchCostSummaryResponse(batchId, totalConsumption, totalOutput, unitCost);
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
        order.getItem() != null ? order.getItem().getId() : null,
        order.getItem() != null ? order.getItem().getName() : null,
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
        batch.getStartedAt(),
        batch.getCompletedAt()
    );
  }

  private ProductionDtos.ProcessRunResponse toRunResponse(ProcessRun run) {
    return new ProductionDtos.ProcessRunResponse(
        run.getId(),
        run.getProductionBatch().getId(),
        run.getProcessStep().getId(),
        run.getProcessStep().getName(),
        run.getRunDate(),
        run.getStatus().name()
    );
  }
}
