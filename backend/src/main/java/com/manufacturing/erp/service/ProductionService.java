package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Company;
import com.manufacturing.erp.domain.Enums.InventoryLocationType;
import com.manufacturing.erp.domain.Enums.LedgerTxnType;
import com.manufacturing.erp.domain.Enums.ProcessInputSourceType;
import com.manufacturing.erp.domain.Enums.ProcessOutputType;
import com.manufacturing.erp.domain.Enums.ProductionStatus;
import com.manufacturing.erp.domain.Enums.StockStatus;
import com.manufacturing.erp.domain.Enums.CalcType;
import com.manufacturing.erp.domain.Enums.PayablePartyType;
import com.manufacturing.erp.domain.DeductionChargeType;
import com.manufacturing.erp.domain.Godown;
import com.manufacturing.erp.domain.InventoryMovement;
import com.manufacturing.erp.domain.Item;
import com.manufacturing.erp.domain.ProcessTemplate;
import com.manufacturing.erp.domain.ProcessTemplateInput;
import com.manufacturing.erp.domain.ProcessTemplateOutput;
import com.manufacturing.erp.domain.ProcessTemplateStep;
import com.manufacturing.erp.domain.ProcessTemplateStep.StepType;
import com.manufacturing.erp.domain.ProcessTemplateStepCharge;
import com.manufacturing.erp.domain.ProductionBatch;
import com.manufacturing.erp.domain.ProductionBatchInput;
import com.manufacturing.erp.domain.ProductionBatchOutput;
import com.manufacturing.erp.domain.ProductionBatchStep;
import com.manufacturing.erp.domain.ProductionOrder;
import com.manufacturing.erp.domain.Uom;
import com.manufacturing.erp.dto.ProductionDtos;
import com.manufacturing.erp.repository.CompanyRepository;
import com.manufacturing.erp.repository.DeductionChargeTypeRepository;
import com.manufacturing.erp.repository.GodownRepository;
import com.manufacturing.erp.repository.InventoryMovementRepository;
import com.manufacturing.erp.repository.ItemRepository;
import com.manufacturing.erp.repository.ProcessTemplateInputRepository;
import com.manufacturing.erp.repository.ProcessTemplateOutputRepository;
import com.manufacturing.erp.repository.ProcessTemplateRepository;
import com.manufacturing.erp.repository.ProcessTemplateStepRepository;
import com.manufacturing.erp.repository.ProcessTemplateStepChargeRepository;
import com.manufacturing.erp.repository.ProductionBatchInputRepository;
import com.manufacturing.erp.repository.ProductionBatchOutputRepository;
import com.manufacturing.erp.repository.ProductionBatchRepository;
import com.manufacturing.erp.repository.ProductionBatchStepRepository;
import com.manufacturing.erp.repository.ProductionOrderRepository;
import com.manufacturing.erp.repository.UomRepository;
import com.manufacturing.erp.security.CompanyContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductionService {
  private final ProcessTemplateRepository processTemplateRepository;
  private final ProcessTemplateInputRepository processTemplateInputRepository;
  private final ProcessTemplateOutputRepository processTemplateOutputRepository;
  private final ProcessTemplateStepRepository processTemplateStepRepository;
  private final ProcessTemplateStepChargeRepository processTemplateStepChargeRepository;
  private final ProductionOrderRepository productionOrderRepository;
  private final ProductionBatchRepository productionBatchRepository;
  private final ProductionBatchInputRepository productionBatchInputRepository;
  private final ProductionBatchOutputRepository productionBatchOutputRepository;
  private final ProductionBatchStepRepository productionBatchStepRepository;
  private final GodownRepository godownRepository;
  private final ItemRepository itemRepository;
  private final UomRepository uomRepository;
  private final InventoryMovementRepository inventoryMovementRepository;
  private final StockLedgerService stockLedgerService;
  private final DeductionChargeTypeRepository deductionChargeTypeRepository;
  private final CompanyRepository companyRepository;
  private final CompanyContext companyContext;

  public ProductionService(ProcessTemplateRepository processTemplateRepository,
                           ProcessTemplateInputRepository processTemplateInputRepository,
                           ProcessTemplateOutputRepository processTemplateOutputRepository,
                           ProcessTemplateStepRepository processTemplateStepRepository,
                           ProcessTemplateStepChargeRepository processTemplateStepChargeRepository,
                           ProductionOrderRepository productionOrderRepository,
                           ProductionBatchRepository productionBatchRepository,
                           ProductionBatchInputRepository productionBatchInputRepository,
                           ProductionBatchOutputRepository productionBatchOutputRepository,
                           ProductionBatchStepRepository productionBatchStepRepository,
                           GodownRepository godownRepository,
                           ItemRepository itemRepository,
                           UomRepository uomRepository,
                           InventoryMovementRepository inventoryMovementRepository,
                           StockLedgerService stockLedgerService,
                           DeductionChargeTypeRepository deductionChargeTypeRepository,
                           CompanyRepository companyRepository,
                           CompanyContext companyContext) {
    this.processTemplateRepository = processTemplateRepository;
    this.processTemplateInputRepository = processTemplateInputRepository;
    this.processTemplateOutputRepository = processTemplateOutputRepository;
    this.processTemplateStepRepository = processTemplateStepRepository;
    this.processTemplateStepChargeRepository = processTemplateStepChargeRepository;
    this.productionOrderRepository = productionOrderRepository;
    this.productionBatchRepository = productionBatchRepository;
    this.productionBatchInputRepository = productionBatchInputRepository;
    this.productionBatchOutputRepository = productionBatchOutputRepository;
    this.productionBatchStepRepository = productionBatchStepRepository;
    this.godownRepository = godownRepository;
    this.itemRepository = itemRepository;
    this.uomRepository = uomRepository;
    this.inventoryMovementRepository = inventoryMovementRepository;
    this.stockLedgerService = stockLedgerService;
    this.deductionChargeTypeRepository = deductionChargeTypeRepository;
    this.companyRepository = companyRepository;
    this.companyContext = companyContext;
  }

  public List<ProductionDtos.ProcessTemplateResponse> listTemplates() {
    Company company = requireCompany();
    return processTemplateRepository.findByCompanyId(company.getId()).stream().map(this::toTemplateResponse).toList();
  }

  public ProductionDtos.ProcessTemplateResponse getTemplate(Long id) {
    return toTemplateResponse(getTemplateOrThrow(id));
  }

  @Transactional
  public ProductionDtos.ProcessTemplateResponse createTemplate(ProductionDtos.ProcessTemplateRequest request) {
    Company company = requireCompany();
    ProcessTemplate template = new ProcessTemplate();
    template.setCompany(company);
    template.setCode(request.code() != null ? request.code() : "PT-" + System.currentTimeMillis());
    template.setName(request.name());
    template.setDescription(request.description());
    template.setOutputItem(request.outputItemId() != null ? fetchItem(request.outputItemId()) : null);
    template.setOutputUom(request.outputUomId() != null ? fetchUom(request.outputUomId()) : null);
    template.setEnabled(request.enabled() != null ? request.enabled() : Boolean.TRUE);
    ProcessTemplate saved = processTemplateRepository.save(template);
    List<ProcessTemplateStep> steps = buildSteps(request.steps(), saved);
    List<ProcessTemplateInput> inputs = buildInputs(request.inputs(), saved);
    List<ProcessTemplateOutput> outputs = buildOutputs(request.outputs(), saved);
    saved.getSteps().clear();
    saved.getSteps().addAll(steps);
    saved.getInputs().clear();
    saved.getInputs().addAll(inputs);
    saved.getOutputs().clear();
    saved.getOutputs().addAll(outputs);
    return toTemplateResponse(processTemplateRepository.save(saved));
  }

  @Transactional
  public ProductionDtos.ProcessTemplateResponse updateTemplate(Long id, ProductionDtos.ProcessTemplateRequest request) {
    ProcessTemplate template = getTemplateOrThrow(id);
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
    template.getOutputs().clear();
    template.getOutputs().addAll(buildOutputs(request.outputs(), template));
    return toTemplateResponse(processTemplateRepository.save(template));
  }

  @Transactional
  public void deleteTemplate(Long id) {
    ProcessTemplate template = getTemplateOrThrow(id);
    processTemplateRepository.delete(template);
  }

  public List<ProductionDtos.ProductionOrderResponse> listOrders() {
    Company company = requireCompany();
    return productionOrderRepository.findByCompanyId(company.getId()).stream().map(this::toOrderResponse).toList();
  }

  public ProductionDtos.ProductionOrderResponse getOrder(Long id) {
    return toOrderResponse(getOrderOrThrow(id));
  }

  @Transactional
  public ProductionDtos.ProductionOrderResponse createOrder(ProductionDtos.ProductionOrderRequest request) {
    Company company = requireCompany();
    ProductionOrder order = new ProductionOrder();
    order.setCompany(company);
    order.setOrderNo(request.orderNo() != null ? request.orderNo() : "ORD-" + System.currentTimeMillis());
    order.setTemplate(fetchTemplate(request.templateId()));
    order.setFinishedItem(fetchItem(request.finishedItemId()));
    order.setUom(fetchUom(request.uomId()));
    order.setPlannedQty(request.plannedQty());
    order.setOrderDate(request.orderDate());
    order.setStatus(ProductionStatus.DRAFT);
    return toOrderResponse(productionOrderRepository.save(order));
  }

  @Transactional
  public ProductionDtos.ProductionOrderResponse updateOrder(Long id, ProductionDtos.ProductionOrderRequest request) {
    ProductionOrder order = getOrderOrThrow(id);
    Company company = requireCompany();
    order.setCompany(company);
    order.setOrderNo(request.orderNo());
    order.setTemplate(fetchTemplate(request.templateId()));
    order.setFinishedItem(fetchItem(request.finishedItemId()));
    order.setUom(fetchUom(request.uomId()));
    order.setPlannedQty(request.plannedQty());
    order.setOrderDate(request.orderDate());
    return toOrderResponse(productionOrderRepository.save(order));
  }

  @Transactional
  public void deleteOrder(Long id) {
    ProductionOrder order = getOrderOrThrow(id);
    productionOrderRepository.delete(order);
  }

  // region batches
  @Transactional
  public ProductionDtos.ProductionBatchResponse createBatch(ProductionDtos.ProductionBatchRequest request) {
    Company company = requireCompany();
    ProcessTemplate template = fetchTemplate(request.templateId());
    ProductionBatch batch = new ProductionBatch();
    batch.setCompany(company);
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
  public List<ProductionDtos.ProductionBatchResponse> listBatches(String q, String status, Long templateId) {
    Company company = requireCompany();
    return productionBatchRepository.findByCompanyId(company.getId()).stream()
        .filter(batch -> q == null || (batch.getBatchNo() != null
            && batch.getBatchNo().toLowerCase().contains(q.toLowerCase())))
        .filter(batch -> status == null || batch.getStatus().name().equalsIgnoreCase(status))
        .filter(batch -> templateId == null || (batch.getTemplate() != null && batch.getTemplate().getId().equals(templateId)))
        .map(this::toBatchResponse)
        .toList();
  }

  @Transactional
  public ProductionDtos.ProductionBatchResponse startBatch(Long batchId) {
    ProductionBatch batch = getBatchOrThrow(batchId);
    batch.setStatus(ProductionStatus.RUNNING);
    batch.setStartedAt(Instant.now());
    if (batch.getStartDate() == null) {
      batch.setStartDate(LocalDate.now());
    }
    productionBatchStepRepository.deleteAll(productionBatchStepRepository.findByBatchIdOrderByStepNoAsc(batchId));
    List<ProductionBatchStep> steps = new ArrayList<>();
    List<ProcessTemplateStep> templateSteps = processTemplateStepRepository.findByTemplateIdOrderByStepNoAsc(batch.getTemplate().getId());
    if (templateSteps.isEmpty()) {
      throw new IllegalStateException("Template steps are required before starting a batch");
    }
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
    return toBatchResponse(getBatchOrThrow(id));
  }

  @Transactional
  public void issueMaterials(Long batchId, ProductionDtos.BatchIssueRequest request) {
    ProductionBatch batch = getBatchOrThrow(batchId);
    if (batch.getStatus() != ProductionStatus.RUNNING) {
      throw new IllegalStateException("Batch must be running to issue materials");
    }
    Integer currentStep = getCurrentStepNo(batchId);
    List<ProductionBatchInput> inputs = new ArrayList<>();
    for (ProductionDtos.BatchInputRequest line : request.inputs()) {
      if (line.stepNo() == null) {
        throw new IllegalArgumentException("Step number is required for inputs");
      }
      enforceStepOrder(currentStep, line.stepNo());
      ProductionBatchInput input = new ProductionBatchInput();
      input.setBatch(batch);
      input.setItem(fetchItem(line.itemId()));
      input.setUom(fetchUom(line.uomId()));
      input.setIssuedQty(line.qty());
      input.setSourceType(ProcessInputSourceType.valueOf(line.sourceType()));
      input.setSourceRefId(line.sourceRefId());
      input.setSourceGodown(line.sourceGodownId() != null ? fetchGodown(line.sourceGodownId()) : null);
      input.setIssuedAt(line.issuedAt() != null ? line.issuedAt() : Instant.now());
      input.setStepNo(line.stepNo());
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
    ProductionBatch batch = getBatchOrThrow(batchId);
    if (batch.getStatus() != ProductionStatus.RUNNING) {
      throw new IllegalStateException("Batch must be running to record output");
    }
    Integer currentStep = getCurrentStepNo(batchId);
    Integer lastStep = getLastStepNo(batchId);
    Integer finalOutputStep = resolveFinalOutputStep(batch);
    List<ProductionBatchOutput> outputs = new ArrayList<>();
    for (ProductionDtos.BatchOutputRequest line : request.outputs()) {
      if (line.stepNo() == null) {
        throw new IllegalArgumentException("Step number is required for outputs");
      }
      enforceStepOrder(currentStep, line.stepNo());
      ProcessOutputType outputType = ProcessOutputType.valueOf(line.outputType());
      if (outputType == ProcessOutputType.FG && finalOutputStep != null && !finalOutputStep.equals(line.stepNo())) {
        throw new IllegalArgumentException("Finished output is only allowed on the final step");
      }
      if (outputType == ProcessOutputType.WIP && finalOutputStep != null && finalOutputStep.equals(line.stepNo())) {
        throw new IllegalArgumentException("WIP output is not allowed on the final step");
      }
      if ((outputType == ProcessOutputType.BYPRODUCT || outputType == ProcessOutputType.EMPTY_BAG)
          && line.destinationGodownId() == null) {
        throw new IllegalArgumentException("Destination godown is required for byproduct/empty bag outputs");
      }
      ProductionBatchOutput output = new ProductionBatchOutput();
      output.setBatch(batch);
      output.setItem(fetchItem(line.itemId()));
      output.setUom(fetchUom(line.uomId()));
      output.setProducedQty(line.qty());
      output.setOutputType(outputType);
      output.setDestinationGodown(line.destinationGodownId() != null ? fetchGodown(line.destinationGodownId()) : null);
      output.setProducedAt(line.producedAt() != null ? line.producedAt() : Instant.now());
      output.setStepNo(line.stepNo());
      outputs.add(output);

      boolean toGodown = output.getOutputType() == ProcessOutputType.FG
          || output.getOutputType() == ProcessOutputType.BYPRODUCT
          || output.getOutputType() == ProcessOutputType.EMPTY_BAG;
      InventoryLocationType locationType = toGodown ? InventoryLocationType.GODOWN : InventoryLocationType.WIP;
      Long locationId = output.getDestinationGodown() != null ? output.getDestinationGodown().getId() : output.getBatch().getId();
      recordMovement("PROD_OUTPUT", batch, output.getItem(), output.getUom(), line.qty(), BigDecimal.ZERO, locationType, locationId);
      if (toGodown && output.getDestinationGodown() != null) {
        stockLedgerService.postEntry("PROD_OUTPUT", batch.getId(), null, LedgerTxnType.IN,
            output.getItem(), output.getUom(), null, null, null, output.getDestinationGodown(), null, null,
            line.qty(), line.qty(), StockStatus.UNRESTRICTED, null, null);
      }
    }
    productionBatchOutputRepository.saveAll(outputs);
    markStepDone(batchId, currentStep);
  }

  @Transactional
  public ProductionDtos.ProductionBatchResponse completeBatch(Long batchId) {
    ProductionBatch batch = getBatchOrThrow(batchId);
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
    getBatchOrThrow(batchId);
    return productionBatchOutputRepository.findByBatchId(batchId).stream()
        .filter(out -> out.getOutputType() == ProcessOutputType.WIP)
        .map(out -> {
          BigDecimal available = out.getProducedQty().subtract(out.getConsumedQty());
          return new ProductionDtos.WipOutputResponse(
              out.getId(),
              out.getBatch().getId(),
              out.getBatch().getBatchNo(),
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
    Company company = requireCompany();
    return productionBatchOutputRepository.findAll().stream()
        .filter(out -> out.getBatch() != null && out.getBatch().getCompany() != null
            && out.getBatch().getCompany().getId().equals(company.getId()))
        .filter(out -> out.getOutputType() == ProcessOutputType.WIP)
        .map(out -> {
          BigDecimal available = out.getProducedQty().subtract(out.getConsumedQty());
          return new ProductionDtos.WipOutputResponse(
              out.getId(),
              out.getBatch().getId(),
              out.getBatch().getBatchNo(),
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
    getBatchOrThrow(batchId);
    List<ProductionBatchInput> inputs = productionBatchInputRepository.findByBatchId(batchId);
    List<ProductionBatchOutput> outputs = productionBatchOutputRepository.findByBatchId(batchId);
    BigDecimal totalConsumption = inputs.stream()
        .filter(input -> input.getSourceType() == ProcessInputSourceType.GODOWN)
        .map(ProductionBatchInput::getIssuedQty)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal totalOutput = outputs.stream()
        .filter(output -> output.getOutputType() == ProcessOutputType.FG)
        .map(ProductionBatchOutput::getProducedQty)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
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
    return getTemplateOrThrow(id);
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
      step.setCharges(buildStepCharges(request.charges(), step));
      steps.add(step);
    }
    steps.sort(Comparator.comparing(ProcessTemplateStep::getStepNo));
    return steps;
  }

  private List<ProcessTemplateStepCharge> buildStepCharges(List<ProductionDtos.ProcessTemplateStepChargeRequest> requests,
                                                           ProcessTemplateStep step) {
    if (requests == null || requests.isEmpty()) {
      return List.of();
    }
    List<ProcessTemplateStepCharge> charges = new ArrayList<>();
    for (ProductionDtos.ProcessTemplateStepChargeRequest request : requests) {
      DeductionChargeType type = deductionChargeTypeRepository.findById(request.chargeTypeId())
          .orElseThrow(() -> new IllegalArgumentException("Charge/Deduction type not found"));
      ProcessTemplateStepCharge charge = new ProcessTemplateStepCharge();
      charge.setStep(step);
      charge.setChargeType(type);
      CalcType calcType = request.calcType() != null ? CalcType.valueOf(request.calcType().toUpperCase())
          : type.getDefaultCalcType();
      charge.setCalcType(calcType);
      charge.setRate(request.rate() != null ? request.rate() : type.getDefaultRate());
      charge.setPerQty(request.perQty() != null ? request.perQty() : Boolean.FALSE);
      charge.setDeduction(request.isDeduction() != null ? request.isDeduction() : type.isDeduction());
      charge.setPayablePartyType(PayablePartyType.valueOf(request.payablePartyType().toUpperCase()));
      charge.setPayablePartyId(request.payablePartyId());
      charge.setRemarks(request.remarks());
      charges.add(charge);
    }
    return charges;
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

  private List<ProcessTemplateOutput> buildOutputs(List<ProductionDtos.ProcessTemplateOutputRequest> requests, ProcessTemplate template) {
    if (requests == null) {
      return List.of();
    }
    List<ProcessTemplateOutput> outputs = new ArrayList<>();
    for (ProductionDtos.ProcessTemplateOutputRequest request : requests) {
      ProcessTemplateOutput output = new ProcessTemplateOutput();
      output.setTemplate(template);
      output.setItem(fetchItem(request.itemId()));
      output.setUom(fetchUom(request.uomId()));
      output.setDefaultRatio(request.defaultRatio());
      output.setOutputType(ProcessOutputType.valueOf(request.outputType()));
      output.setNotes(request.notes());
      outputs.add(output);
    }
    return outputs;
  }

  private ProductionDtos.ProcessTemplateResponse toTemplateResponse(ProcessTemplate template) {
    List<ProductionDtos.ProcessTemplateStepResponse> steps = processTemplateStepRepository
        .findByTemplateIdOrderByStepNoAsc(template.getId()).stream()
        .map(step -> {
          List<ProductionDtos.ProcessTemplateStepChargeResponse> charges = processTemplateStepChargeRepository
              .findByStepId(step.getId()).stream()
              .map(charge -> new ProductionDtos.ProcessTemplateStepChargeResponse(
                  charge.getId(),
                  charge.getChargeType() != null ? charge.getChargeType().getId() : null,
                  charge.getChargeType() != null ? charge.getChargeType().getName() : null,
                  charge.getCalcType() != null ? charge.getCalcType().name() : null,
                  charge.getRate(),
                  charge.isPerQty(),
                  charge.isDeduction(),
                  charge.getPayablePartyType() != null ? charge.getPayablePartyType().name() : null,
                  charge.getPayablePartyId(),
                  charge.getRemarks()
              )).toList();
          return new ProductionDtos.ProcessTemplateStepResponse(
              step.getId(),
              step.getStepNo(),
              step.getStepName(),
              step.getStepType() != null ? step.getStepType().name() : null,
              step.getNotes(),
              charges
          );
        }).toList();
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
    List<ProductionDtos.ProcessTemplateOutputResponse> outputs = processTemplateOutputRepository.findByTemplateId(template.getId()).stream()
        .map(output -> new ProductionDtos.ProcessTemplateOutputResponse(
            output.getId(),
            output.getItem().getId(),
            output.getItem().getName(),
            output.getUom().getId(),
            output.getUom().getCode(),
            output.getDefaultRatio(),
            output.getOutputType().name(),
            output.getNotes()
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
        outputs,
        steps
    );
  }

  private ProductionDtos.ProductionOrderResponse toOrderResponse(ProductionOrder order) {
    return new ProductionDtos.ProductionOrderResponse(
        order.getId(),
        order.getOrderNo(),
        order.getTemplate() != null ? order.getTemplate().getId() : null,
        order.getTemplate() != null ? order.getTemplate().getName() : null,
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
            input.getStepNo(),
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
            output.getStepNo(),
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
    movement.setCompany(batch.getCompany());
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

  private BigDecimal defaultZero(BigDecimal value) {
    return value != null ? value : BigDecimal.ZERO;
  }

  private Integer getCurrentStepNo(Long batchId) {
    return productionBatchStepRepository.findByBatchIdOrderByStepNoAsc(batchId).stream()
        .filter(step -> step.getStatus() == ProductionBatchStep.StepStatus.PENDING)
        .map(ProductionBatchStep::getStepNo)
        .findFirst()
        .orElse(null);
  }

  private Integer getLastStepNo(Long batchId) {
    return productionBatchStepRepository.findByBatchIdOrderByStepNoAsc(batchId).stream()
        .map(ProductionBatchStep::getStepNo)
        .max(Integer::compareTo)
        .orElse(null);
  }

  private Integer resolveFinalOutputStep(ProductionBatch batch) {
    if (batch == null || batch.getTemplate() == null) {
      return null;
    }
    Integer lastProduceStep = processTemplateStepRepository.findByTemplateIdOrderByStepNoAsc(batch.getTemplate().getId()).stream()
        .filter(step -> step.getStepType() == StepType.PRODUCE)
        .map(ProcessTemplateStep::getStepNo)
        .max(Integer::compareTo)
        .orElse(null);
    return lastProduceStep != null ? lastProduceStep : getLastStepNo(batch.getId());
  }

  private void enforceStepOrder(Integer currentStep, Integer requestedStep) {
    if (currentStep == null) {
      throw new IllegalStateException("No pending steps available for this batch");
    }
    if (!currentStep.equals(requestedStep)) {
      throw new IllegalStateException("Complete step " + currentStep + " before working on step " + requestedStep);
    }
  }

  private void markStepDone(Long batchId, Integer stepNo) {
    if (stepNo == null) {
      return;
    }
    ProductionBatchStep step = productionBatchStepRepository.findByBatchIdOrderByStepNoAsc(batchId).stream()
        .filter(s -> s.getStepNo().equals(stepNo))
        .findFirst()
        .orElse(null);
    if (step != null && step.getStatus() == ProductionBatchStep.StepStatus.PENDING) {
      step.setStatus(ProductionBatchStep.StepStatus.DONE);
      step.setStartedAt(step.getStartedAt() != null ? step.getStartedAt() : Instant.now());
      step.setCompletedAt(Instant.now());
      productionBatchStepRepository.save(step);
    }
  }

  private ProcessTemplate getTemplateOrThrow(Long id) {
    Company company = requireCompany();
    return processTemplateRepository.findByIdAndCompanyId(id, company.getId())
        .orElseThrow(() -> new IllegalArgumentException("Process template not found"));
  }

  private ProductionOrder getOrderOrThrow(Long id) {
    Company company = requireCompany();
    return productionOrderRepository.findByIdAndCompanyId(id, company.getId())
        .orElseThrow(() -> new IllegalArgumentException("Production order not found"));
  }

  private ProductionBatch getBatchOrThrow(Long id) {
    Company company = requireCompany();
    return productionBatchRepository.findByIdAndCompanyId(id, company.getId())
        .orElseThrow(() -> new IllegalArgumentException("Batch not found"));
  }

  private Company requireCompany() {
    Long companyId = companyContext.getCompanyId();
    if (companyId == null) {
      throw new IllegalArgumentException("Missing company context");
    }
    return companyRepository.findById(companyId)
        .orElseThrow(() -> new IllegalArgumentException("Company not found"));
  }
}
