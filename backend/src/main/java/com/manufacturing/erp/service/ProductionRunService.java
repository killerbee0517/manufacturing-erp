package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Company;
import com.manufacturing.erp.domain.Broker;
import com.manufacturing.erp.domain.DeductionChargeType;
import com.manufacturing.erp.domain.Enums.CalcType;
import com.manufacturing.erp.domain.Enums.InventoryLocationType;
import com.manufacturing.erp.domain.Enums.LedgerTxnType;
import com.manufacturing.erp.domain.Enums.LedgerType;
import com.manufacturing.erp.domain.Enums.PayablePartyType;
import com.manufacturing.erp.domain.Enums.ProcessInputSourceType;
import com.manufacturing.erp.domain.Enums.ProcessOutputType;
import com.manufacturing.erp.domain.Enums.ProductionStatus;
import com.manufacturing.erp.domain.ExpenseParty;
import com.manufacturing.erp.domain.Godown;
import com.manufacturing.erp.domain.InventoryMovement;
import com.manufacturing.erp.domain.Item;
import com.manufacturing.erp.domain.Ledger;
import com.manufacturing.erp.domain.ProcessRun;
import com.manufacturing.erp.domain.ProcessRunCharge;
import com.manufacturing.erp.domain.ProcessRunConsumption;
import com.manufacturing.erp.domain.ProcessRunOutput;
import com.manufacturing.erp.domain.ProcessTemplateStep;
import com.manufacturing.erp.domain.ProcessTemplateStepCharge;
import com.manufacturing.erp.domain.ProductionBatch;
import com.manufacturing.erp.domain.Supplier;
import com.manufacturing.erp.domain.Uom;
import com.manufacturing.erp.dto.ProductionDtos;
import com.manufacturing.erp.repository.BrokerRepository;
import com.manufacturing.erp.repository.CompanyRepository;
import com.manufacturing.erp.repository.DeductionChargeTypeRepository;
import com.manufacturing.erp.repository.ExpensePartyRepository;
import com.manufacturing.erp.repository.GodownRepository;
import com.manufacturing.erp.repository.InventoryMovementRepository;
import com.manufacturing.erp.repository.ItemRepository;
import com.manufacturing.erp.repository.ProcessRunChargeRepository;
import com.manufacturing.erp.repository.ProcessRunConsumptionRepository;
import com.manufacturing.erp.repository.ProcessRunOutputRepository;
import com.manufacturing.erp.repository.ProcessRunRepository;
import com.manufacturing.erp.repository.ProcessTemplateStepRepository;
import com.manufacturing.erp.repository.ProcessTemplateStepChargeRepository;
import com.manufacturing.erp.repository.ProductionBatchRepository;
import com.manufacturing.erp.repository.SupplierRepository;
import com.manufacturing.erp.repository.UomRepository;
import com.manufacturing.erp.repository.VehicleRepository;
import com.manufacturing.erp.security.CompanyContext;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductionRunService {
  private final ProductionBatchRepository productionBatchRepository;
  private final ProcessRunRepository processRunRepository;
  private final ProcessRunConsumptionRepository processRunConsumptionRepository;
  private final ProcessRunOutputRepository processRunOutputRepository;
  private final ProcessRunChargeRepository processRunChargeRepository;
  private final ProcessTemplateStepRepository processTemplateStepRepository;
  private final ProcessTemplateStepChargeRepository processTemplateStepChargeRepository;
  private final ItemRepository itemRepository;
  private final UomRepository uomRepository;
  private final GodownRepository godownRepository;
  private final InventoryMovementRepository inventoryMovementRepository;
  private final StockLedgerService stockLedgerService;
  private final DeductionChargeTypeRepository deductionChargeTypeRepository;
  private final SupplierRepository supplierRepository;
  private final BrokerRepository brokerRepository;
  private final VehicleRepository vehicleRepository;
  private final ExpensePartyRepository expensePartyRepository;
  private final LedgerService ledgerService;
  private final VoucherService voucherService;
  private final CompanyRepository companyRepository;
  private final CompanyContext companyContext;

  public ProductionRunService(ProductionBatchRepository productionBatchRepository,
                              ProcessRunRepository processRunRepository,
                              ProcessRunConsumptionRepository processRunConsumptionRepository,
                              ProcessRunOutputRepository processRunOutputRepository,
                              ProcessRunChargeRepository processRunChargeRepository,
                              ProcessTemplateStepRepository processTemplateStepRepository,
                              ProcessTemplateStepChargeRepository processTemplateStepChargeRepository,
                              ItemRepository itemRepository,
                              UomRepository uomRepository,
                              GodownRepository godownRepository,
                              InventoryMovementRepository inventoryMovementRepository,
                              StockLedgerService stockLedgerService,
                              DeductionChargeTypeRepository deductionChargeTypeRepository,
                              SupplierRepository supplierRepository,
                              BrokerRepository brokerRepository,
                              VehicleRepository vehicleRepository,
                              ExpensePartyRepository expensePartyRepository,
                              LedgerService ledgerService,
                              VoucherService voucherService,
                              CompanyRepository companyRepository,
                              CompanyContext companyContext) {
    this.productionBatchRepository = productionBatchRepository;
    this.processRunRepository = processRunRepository;
    this.processRunConsumptionRepository = processRunConsumptionRepository;
    this.processRunOutputRepository = processRunOutputRepository;
    this.processRunChargeRepository = processRunChargeRepository;
    this.processTemplateStepRepository = processTemplateStepRepository;
    this.processTemplateStepChargeRepository = processTemplateStepChargeRepository;
    this.itemRepository = itemRepository;
    this.uomRepository = uomRepository;
    this.godownRepository = godownRepository;
    this.inventoryMovementRepository = inventoryMovementRepository;
    this.stockLedgerService = stockLedgerService;
    this.deductionChargeTypeRepository = deductionChargeTypeRepository;
    this.supplierRepository = supplierRepository;
    this.brokerRepository = brokerRepository;
    this.vehicleRepository = vehicleRepository;
    this.expensePartyRepository = expensePartyRepository;
    this.ledgerService = ledgerService;
    this.voucherService = voucherService;
    this.companyRepository = companyRepository;
    this.companyContext = companyContext;
  }

  @Transactional
  public ProductionDtos.ProductionRunResponse createRun(Long batchId, ProductionDtos.ProductionRunRequest request) {
    ProductionBatch batch = fetchBatch(batchId);
    ProcessRun run = new ProcessRun();
    run.setCompany(requireCompany());
    run.setProductionBatch(batch);
    run.setRunNo(nextRunNo(batchId));
    applyStepMetadata(run, request);
    run.setRunDate(request.runDate() != null ? request.runDate() : LocalDate.now());
    run.setStatus(ProductionStatus.DRAFT);
    run.setNotes(request.notes());
    run.setMoisturePercent(request.moisturePercent());
    ProcessRun saved = processRunRepository.save(run);

    List<ProcessRunConsumption> inputs = buildConsumptions(saved, request.inputs());
    List<ProcessRunOutput> outputs = buildOutputs(saved, request.outputs());
    processRunConsumptionRepository.saveAll(inputs);
    processRunOutputRepository.saveAll(outputs);
    BigDecimal baseAmount = resolveBaseAmountFromOutputs(request.outputs());
    List<ProcessRunCharge> charges = buildCharges(saved, request.charges(), baseAmount);
    if (!charges.isEmpty()) {
      processRunChargeRepository.saveAll(charges);
    }
    return toRunResponse(saved);
  }

  @Transactional
  public ProductionDtos.ProductionRunResponse updateRun(Long runId, ProductionDtos.ProductionRunRequest request) {
    ProcessRun run = getRunOrThrow(runId);
    applyStepMetadata(run, request);
    run.setRunDate(request.runDate() != null ? request.runDate() : run.getRunDate());
    run.setNotes(request.notes());
    run.setMoisturePercent(request.moisturePercent());
    processRunConsumptionRepository.deleteAll(processRunConsumptionRepository.findByProcessRunId(runId));
    processRunOutputRepository.deleteAll(processRunOutputRepository.findByProcessRunId(runId));
    processRunChargeRepository.deleteAll(processRunChargeRepository.findByProcessRunId(runId));
    processRunConsumptionRepository.saveAll(buildConsumptions(run, request.inputs()));
    processRunOutputRepository.saveAll(buildOutputs(run, request.outputs()));
    BigDecimal baseAmount = resolveBaseAmountFromOutputs(request.outputs());
    List<ProcessRunCharge> charges = buildCharges(run, request.charges(), baseAmount);
    if (!charges.isEmpty()) {
      processRunChargeRepository.saveAll(charges);
    }
    return toRunResponse(processRunRepository.save(run));
  }

  @Transactional(readOnly = true)
  public ProductionDtos.ProductionRunResponse getRun(Long id) {
    return toRunResponse(getRunOrThrow(id));
  }

  @Transactional(readOnly = true)
  public ProductionDtos.RunCostSummaryResponse getRunCostSummary(Long runId) {
    ProcessRun run = getRunOrThrow(runId);
    List<ProcessRunConsumption> inputs = processRunConsumptionRepository.findByProcessRunId(runId);
    List<ProcessRunOutput> outputs = processRunOutputRepository.findByProcessRunId(runId);
    List<ProcessRunCharge> charges = processRunChargeRepository.findByProcessRunId(runId);

    BigDecimal totalInputQty = inputs.stream()
        .map(ProcessRunConsumption::getQuantity)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal totalInputAmount = inputs.stream()
        .map(input -> resolveAmount(input.getAmount(), input.getRate(), input.getQuantity()))
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal allocatableOutputQty = outputs.stream()
        .filter(out -> out.getOutputType() != ProcessOutputType.BYPRODUCT
            && out.getOutputType() != ProcessOutputType.EMPTY_BAG)
        .map(ProcessRunOutput::getQuantity)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal unitCost = allocatableOutputQty.compareTo(BigDecimal.ZERO) > 0
        ? totalInputAmount.divide(allocatableOutputQty, 4, java.math.RoundingMode.HALF_UP)
        : BigDecimal.ZERO;

    List<ProductionDtos.RunCostSummaryLine> lines = outputs.stream().map(output -> {
      boolean allocatable = output.getOutputType() != ProcessOutputType.BYPRODUCT
          && output.getOutputType() != ProcessOutputType.EMPTY_BAG;
      BigDecimal lineUnitCost = allocatable ? unitCost : BigDecimal.ZERO;
      BigDecimal lineAmount = lineUnitCost.multiply(defaultZero(output.getQuantity()));
      return new ProductionDtos.RunCostSummaryLine(
          output.getId(),
          output.getItem().getId(),
          output.getItem().getName(),
          output.getOutputType().name(),
          output.getQuantity(),
          lineUnitCost,
          lineAmount
      );
    }).toList();

    BigDecimal totalOutputAmount = lines.stream()
        .map(ProductionDtos.RunCostSummaryLine::amount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal yieldPercent = BigDecimal.ZERO;
    BigDecimal shrinkPercent = BigDecimal.ZERO;
    if (totalInputQty.compareTo(BigDecimal.ZERO) > 0) {
      yieldPercent = allocatableOutputQty
          .multiply(BigDecimal.valueOf(100))
          .divide(totalInputQty, 4, java.math.RoundingMode.HALF_UP);
      shrinkPercent = totalInputQty.subtract(allocatableOutputQty)
          .multiply(BigDecimal.valueOf(100))
          .divide(totalInputQty, 4, java.math.RoundingMode.HALF_UP);
    }

    return new ProductionDtos.RunCostSummaryResponse(
        runId,
        totalInputQty,
        totalInputAmount,
        allocatableOutputQty,
        totalOutputAmount,
        yieldPercent,
        run.getMoisturePercent(),
        shrinkPercent,
        unitCost,
        lines
    );
  }

  @Transactional(readOnly = true)
  public List<ProductionDtos.ProductionRunResponse> listRunsForBatch(Long batchId) {
    Company company = requireCompany();
    fetchBatch(batchId);
    return processRunRepository.findByProductionBatchIdAndCompanyId(batchId, company.getId()).stream()
        .sorted(Comparator.comparing(ProcessRun::getRunNo, Comparator.nullsLast(Integer::compareTo))
            .thenComparing(ProcessRun::getId))
        .map(this::toRunResponse)
        .toList();
  }

  @Transactional
  public ProductionDtos.ProductionRunResponse postRun(Long runId) {
    ProcessRun run = getRunOrThrow(runId);
    if (run.getStatus() == ProductionStatus.COMPLETED) {
      return toRunResponse(run);
    }
    List<ProcessRunConsumption> inputs = processRunConsumptionRepository.findByProcessRunId(runId);
    List<ProcessRunOutput> outputs = processRunOutputRepository.findByProcessRunId(runId);
    List<ProcessRunCharge> charges = processRunChargeRepository.findByProcessRunId(runId);

    for (ProcessRunConsumption input : inputs) {
      validateQuantity(input.getQuantity(), "Input quantity");
      if (input.getSourceType() == ProcessInputSourceType.GODOWN) {
        if (input.getSourceGodown() == null) {
          throw new IllegalArgumentException("Godown is required for GODOWN inputs");
        }
        recordMovement("PROD_RUN_IN", run, input.getItem(), input.getUom(), BigDecimal.ZERO, input.getQuantity(),
            InventoryLocationType.GODOWN, input.getSourceGodown().getId());
        stockLedgerService.postEntry(
            "PROD_RUN",
            run.getId(),
            input.getId(),
            LedgerTxnType.OUT,
            input.getItem(),
            input.getUom(),
            null,
            null,
            input.getSourceGodown(),
            null,
            null,
            run.getProductionBatch().getId(),
            input.getQuantity(),
            input.getQuantity(),
            com.manufacturing.erp.domain.Enums.StockStatus.UNRESTRICTED,
            input.getRate(),
            input.getAmount());
      } else {
        ProcessRunOutput source = input.getSourceRunOutput();
        if (source == null) {
          throw new IllegalArgumentException("WIP input must reference a source output");
        }
        BigDecimal available = defaultZero(source.getQuantity()).subtract(defaultZero(source.getConsumedQuantity()));
        if (available.compareTo(input.getQuantity()) < 0) {
          throw new IllegalArgumentException("Insufficient WIP quantity for " + source.getItem().getName());
        }
        source.setConsumedQuantity(defaultZero(source.getConsumedQuantity()).add(input.getQuantity()));
        processRunOutputRepository.save(source);
        recordMovement("PROD_RUN_IN", run, input.getItem(), input.getUom(), BigDecimal.ZERO, input.getQuantity(),
            InventoryLocationType.WIP, source.getId());
      }
    }

    for (ProcessRunOutput output : outputs) {
      validateQuantity(output.getQuantity(), "Output quantity");
      InventoryLocationType locationType = InventoryLocationType.WIP;
      Long locationId = output.getProcessRun().getId();
      boolean postToGodown = output.getOutputType() == ProcessOutputType.FG
          || output.getOutputType() == ProcessOutputType.BYPRODUCT
          || output.getOutputType() == ProcessOutputType.EMPTY_BAG;
      if (postToGodown && output.getDestGodown() == null) {
        throw new IllegalArgumentException("Destination godown is required for non-WIP outputs");
      }
      if (postToGodown) {
        locationType = InventoryLocationType.GODOWN;
        locationId = output.getDestGodown() != null ? output.getDestGodown().getId() : null;
        recordMovement("PROD_RUN_OUT", run, output.getItem(), output.getUom(), output.getQuantity(), BigDecimal.ZERO,
            locationType, locationId);
        stockLedgerService.postEntry(
            "PROD_RUN",
            run.getId(),
            output.getId(),
            LedgerTxnType.IN,
            output.getItem(),
            output.getUom(),
            null,
            null,
            null,
            output.getDestGodown(),
            output.getDestGodown(),
            run.getProductionBatch().getId(),
            output.getQuantity(),
            output.getQuantity(),
            com.manufacturing.erp.domain.Enums.StockStatus.UNRESTRICTED,
            output.getRate(),
            output.getAmount());
      } else {
        recordMovement("PROD_RUN_OUT", run, output.getItem(), output.getUom(), output.getQuantity(), BigDecimal.ZERO,
            locationType, locationId);
      }
    }

    List<VoucherService.VoucherLineRequest> postings = new java.util.ArrayList<>();
    if (!charges.isEmpty()) {
      Ledger chargesLedger = ledgerService.findOrCreateLedger("Production Charges", LedgerType.EXPENSE);
      Ledger deductionsLedger = ledgerService.findOrCreateLedger("Production Deductions", LedgerType.GENERAL);
      postings.addAll(buildChargePostings(charges, chargesLedger, deductionsLedger));
    }
    postings.addAll(buildTemplateStepChargePostings(run, outputs));
    if (!postings.isEmpty()) {
      voucherService.createVoucher("PRODUCTION_RUN", run.getId(), run.getRunDate(),
          "Production run charges", postings);
    }

    run.setStatus(ProductionStatus.COMPLETED);
    run.setStartedAt(run.getStartedAt() != null ? run.getStartedAt() : Instant.now());
    run.setEndedAt(Instant.now());
    return toRunResponse(processRunRepository.save(run));
  }

  @Transactional(readOnly = true)
  public List<ProductionDtos.WipSelectionResponse> listAvailableWipForBatch(Long batchId) {
    fetchBatch(batchId);
    return processRunOutputRepository.findByProcessRunProductionBatchId(batchId).stream()
        .filter(out -> out.getOutputType() == ProcessOutputType.WIP)
        .filter(out -> out.getProcessRun().getStatus() == ProductionStatus.COMPLETED)
        .map(this::toWipSelection)
        .filter(res -> res.availableQuantity().compareTo(BigDecimal.ZERO) > 0)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ProductionDtos.WipSelectionResponse> searchWip(String search) {
    Company company = requireCompany();
    return processRunOutputRepository.findByProcessRunCompanyId(company.getId()).stream()
        .filter(out -> out.getOutputType() == ProcessOutputType.WIP)
        .filter(out -> out.getProcessRun().getStatus() == ProductionStatus.COMPLETED)
        .filter(out -> search == null
            || out.getItem().getName().toLowerCase().contains(search.toLowerCase())
            || (out.getItem().getSku() != null
            && out.getItem().getSku().toLowerCase().contains(search.toLowerCase())))
        .map(this::toWipSelection)
        .filter(res -> res.availableQuantity().compareTo(BigDecimal.ZERO) > 0)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ProductionDtos.WipOutputResponse> listWipBalances() {
    Company company = requireCompany();
    return processRunOutputRepository.findByProcessRunCompanyId(company.getId()).stream()
        .filter(out -> out.getOutputType() == ProcessOutputType.WIP)
        .filter(out -> out.getProcessRun().getStatus() == ProductionStatus.COMPLETED)
        .map(out -> {
          BigDecimal available = defaultZero(out.getQuantity()).subtract(defaultZero(out.getConsumedQuantity()));
          ProductionBatch batch = out.getProcessRun().getProductionBatch();
          return new ProductionDtos.WipOutputResponse(
              out.getId(),
              batch != null ? batch.getId() : null,
              batch != null ? batch.getBatchNo() : null,
              out.getItem().getId(),
              out.getItem().getName(),
              out.getUom().getId(),
              out.getUom().getCode(),
              out.getQuantity(),
              defaultZero(out.getConsumedQuantity()),
              available
          );
        })
        .filter(res -> res.availableQuantity().compareTo(BigDecimal.ZERO) > 0)
        .toList();
  }

  private void applyStepMetadata(ProcessRun run, ProductionDtos.ProductionRunRequest request) {
    run.setStepNo(request.stepNo());
    if (request.stepNo() != null && run.getProductionBatch().getTemplate() != null) {
      List<ProcessTemplateStep> steps = processTemplateStepRepository.findByTemplateIdOrderByStepNoAsc(
          run.getProductionBatch().getTemplate().getId());
      ProcessTemplateStep matched = steps.stream()
          .filter(step -> Objects.equals(step.getStepNo(), request.stepNo()))
          .findFirst()
          .orElse(null);
      if (matched != null) {
        run.setStepName(matched.getStepName());
      }
    }
    if (request.stepName() != null) {
      run.setStepName(request.stepName());
    }
  }

  private List<ProcessRunConsumption> buildConsumptions(ProcessRun run, List<ProductionDtos.RunInputRequest> inputs) {
    if (inputs == null) {
      return List.of();
    }
    return inputs.stream().map(req -> {
      ProcessRunConsumption consumption = new ProcessRunConsumption();
      consumption.setProcessRun(run);
      consumption.setItem(fetchItem(req.itemId()));
      consumption.setUom(fetchUom(req.uomId()));
      consumption.setQuantity(req.qty());
      consumption.setSourceType(ProcessInputSourceType.valueOf(req.sourceType()));
      if (consumption.getSourceType() == ProcessInputSourceType.GODOWN) {
        consumption.setSourceGodown(fetchGodown(req.godownId()));
      } else if (req.sourceRefId() != null) {
        ProcessRunOutput source = processRunOutputRepository.findById(req.sourceRefId())
            .orElseThrow(() -> new IllegalArgumentException("Referenced WIP output not found"));
        Company company = requireCompany();
        if (source.getProcessRun() == null || source.getProcessRun().getCompany() == null
            || !source.getProcessRun().getCompany().getId().equals(company.getId())) {
          throw new IllegalArgumentException("Referenced WIP output not found");
        }
        consumption.setSourceRunOutput(source);
      }
      consumption.setRate(defaultZero(req.rate()));
      consumption.setAmount(resolveAmount(req.amount(), req.rate(), req.qty()));
      return consumption;
    }).toList();
  }

  private List<ProcessRunOutput> buildOutputs(ProcessRun run, List<ProductionDtos.RunOutputRequest> outputs) {
    if (outputs == null) {
      return List.of();
    }
    return outputs.stream().map(req -> {
      ProcessRunOutput output = new ProcessRunOutput();
      output.setProcessRun(run);
      output.setItem(fetchItem(req.itemId()));
      output.setUom(fetchUom(req.uomId()));
      output.setQuantity(req.qty());
      output.setOutputType(ProcessOutputType.valueOf(req.outputType()));
      if ((output.getOutputType() == ProcessOutputType.FG
          || output.getOutputType() == ProcessOutputType.BYPRODUCT
          || output.getOutputType() == ProcessOutputType.EMPTY_BAG)
          && req.destGodownId() != null) {
        output.setDestGodown(fetchGodown(req.destGodownId()));
      }
      output.setConsumedQuantity(BigDecimal.ZERO);
      output.setRate(defaultZero(req.rate()));
      output.setAmount(resolveAmount(req.amount(), req.rate(), req.qty()));
      return output;
    }).toList();
  }

  private BigDecimal resolveBaseAmountFromOutputs(List<ProductionDtos.RunOutputRequest> outputs) {
    if (outputs == null) {
      return BigDecimal.ZERO;
    }
    return outputs.stream()
        .map(req -> resolveAmount(req.amount(), req.rate(), req.qty()))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private List<ProcessRunCharge> buildCharges(ProcessRun run,
                                              List<ProductionDtos.RunChargeRequest> requests,
                                              BigDecimal baseAmount) {
    if (requests == null || requests.isEmpty()) {
      return List.of();
    }
    return requests.stream().map(req -> {
      DeductionChargeType type = deductionChargeTypeRepository.findById(req.chargeTypeId())
          .orElseThrow(() -> new IllegalArgumentException("Charge/Deduction type not found"));
      CalcType calcType = req.calcType() != null ? CalcType.valueOf(req.calcType().toUpperCase())
          : type.getDefaultCalcType();
      BigDecimal rate = req.rate() != null ? req.rate() : type.getDefaultRate();
      BigDecimal quantity = req.quantity();
      BigDecimal amount = req.amount();
      if (amount == null) {
        if (calcType == CalcType.PERCENT && rate != null) {
          amount = baseAmount.multiply(rate).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        } else if (rate != null && quantity != null) {
          amount = rate.multiply(quantity).setScale(2, java.math.RoundingMode.HALF_UP);
        } else if (rate != null) {
          amount = rate.setScale(2, java.math.RoundingMode.HALF_UP);
        } else {
          amount = BigDecimal.ZERO;
        }
      }
      boolean isDeduction = req.isDeduction() != null ? req.isDeduction() : type.isDeduction();

      ProcessRunCharge charge = new ProcessRunCharge();
      charge.setProcessRun(run);
      charge.setChargeType(type);
      charge.setCalcType(calcType);
      charge.setRate(rate);
      charge.setQuantity(quantity);
      charge.setAmount(amount);
      charge.setDeduction(isDeduction);
      charge.setPayablePartyType(PayablePartyType.valueOf(req.payablePartyType().toUpperCase()));
      charge.setPayablePartyId(req.payablePartyId());
      charge.setRemarks(req.remarks());
      return charge;
    }).toList();
  }

  private List<VoucherService.VoucherLineRequest> buildChargePostings(List<ProcessRunCharge> charges,
                                                                      Ledger chargeLedger,
                                                                      Ledger deductionLedger) {
    if (charges == null || charges.isEmpty()) {
      return List.of();
    }
    List<VoucherService.VoucherLineRequest> postings = new java.util.ArrayList<>();
    for (ProcessRunCharge charge : charges) {
      BigDecimal amount = defaultZero(charge.getAmount());
      if (amount.compareTo(BigDecimal.ZERO) == 0) {
        continue;
      }
      Ledger partyLedger = ledgerForParty(charge.getPayablePartyType(), charge.getPayablePartyId());
      Ledger targetLedger = partyLedger != null ? partyLedger : (charge.isDeduction() ? deductionLedger : chargeLedger);
      if (charge.isDeduction()) {
        postings.add(new VoucherService.VoucherLineRequest(targetLedger, BigDecimal.ZERO, amount));
      } else {
        postings.add(new VoucherService.VoucherLineRequest(targetLedger, amount, BigDecimal.ZERO));
      }
    }
    return postings;
  }

  private List<VoucherService.VoucherLineRequest> buildTemplateStepChargePostings(ProcessRun run,
                                                                                  List<ProcessRunOutput> outputs) {
    if (run.getProductionBatch() == null || run.getProductionBatch().getTemplate() == null || run.getStepNo() == null) {
      return List.of();
    }
    List<ProcessTemplateStep> steps = processTemplateStepRepository.findByTemplateIdOrderByStepNoAsc(
        run.getProductionBatch().getTemplate().getId());
    ProcessTemplateStep step = steps.stream()
        .filter(candidate -> Objects.equals(candidate.getStepNo(), run.getStepNo()))
        .findFirst()
        .orElse(null);
    if (step == null) {
      return List.of();
    }
    List<ProcessTemplateStepCharge> stepCharges = processTemplateStepChargeRepository.findByStepId(step.getId());
    if (stepCharges.isEmpty()) {
      return List.of();
    }
    BigDecimal baseAmount = outputs.stream()
        .map(output -> resolveAmount(output.getAmount(), output.getRate(), output.getQuantity()))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal outputQty = outputs.stream()
        .filter(out -> out.getOutputType() != ProcessOutputType.BYPRODUCT
            && out.getOutputType() != ProcessOutputType.EMPTY_BAG)
        .map(out -> defaultZero(out.getQuantity()))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    Ledger chargesLedger = ledgerService.findOrCreateLedger("Production Charges", LedgerType.EXPENSE);
    Ledger deductionsLedger = ledgerService.findOrCreateLedger("Production Deductions", LedgerType.GENERAL);
    List<VoucherService.VoucherLineRequest> postings = new java.util.ArrayList<>();
    for (ProcessTemplateStepCharge charge : stepCharges) {
      DeductionChargeType type = charge.getChargeType();
      CalcType calcType = charge.getCalcType() != null ? charge.getCalcType()
          : (type != null ? type.getDefaultCalcType() : CalcType.FLAT);
      BigDecimal rate = charge.getRate() != null ? charge.getRate() : (type != null ? type.getDefaultRate() : null);
      boolean isDeduction = charge.isDeduction() || (type != null && type.isDeduction());
      BigDecimal amount;
      if (calcType == CalcType.PERCENT && rate != null) {
        amount = baseAmount.multiply(rate).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
      } else if (charge.isPerQty() && rate != null) {
        amount = rate.multiply(outputQty).setScale(2, java.math.RoundingMode.HALF_UP);
      } else if (rate != null) {
        amount = rate.setScale(2, java.math.RoundingMode.HALF_UP);
      } else {
        amount = BigDecimal.ZERO;
      }
      if (amount.compareTo(BigDecimal.ZERO) == 0) {
        continue;
      }
      Ledger partyLedger = ledgerForParty(charge.getPayablePartyType(), charge.getPayablePartyId());
      Ledger targetLedger = partyLedger != null ? partyLedger : (isDeduction ? deductionsLedger : chargesLedger);
      if (isDeduction) {
        postings.add(new VoucherService.VoucherLineRequest(targetLedger, BigDecimal.ZERO, amount));
      } else {
        postings.add(new VoucherService.VoucherLineRequest(targetLedger, amount, BigDecimal.ZERO));
      }
    }
    return postings;
  }

  private Ledger ledgerForParty(PayablePartyType partyType, Long partyId) {
    if (partyType == null || partyId == null) {
      return null;
    }
    return switch (partyType) {
      case SUPPLIER -> supplierRepository.findById(partyId)
          .map(supplier -> {
            if (supplier.getLedger() == null) {
              supplier.setLedger(ledgerService.createLedger(supplier.getName(), LedgerType.SUPPLIER, "SUPPLIER", supplier.getId()));
              supplierRepository.save(supplier);
            }
            return supplier.getLedger();
          })
          .orElse(null);
      case BROKER -> brokerRepository.findById(partyId)
          .map(broker -> ledgerService.findOrCreateLedger("Broker " + broker.getName(), LedgerType.GENERAL))
          .orElse(null);
      case VEHICLE -> vehicleRepository.findById(partyId)
          .map(vehicle -> ledgerService.findOrCreateLedger("Vehicle " + vehicle.getVehicleNo(), LedgerType.EXPENSE))
          .orElse(null);
      case EXPENSE -> expensePartyRepository.findById(partyId)
          .map(party -> {
            if (party.getLedger() == null) {
              party.setLedger(ledgerService.findOrCreateLedger(party.getName(), LedgerType.EXPENSE));
              expensePartyRepository.save(party);
            }
            return party.getLedger();
          })
          .orElse(null);
      default -> null;
    };
  }

  private ProductionDtos.ProductionRunResponse toRunResponse(ProcessRun run) {
    List<ProcessRunConsumption> inputs = processRunConsumptionRepository.findByProcessRunId(run.getId());
    List<ProcessRunOutput> outputs = processRunOutputRepository.findByProcessRunId(run.getId());
    List<ProcessRunCharge> charges = processRunChargeRepository.findByProcessRunId(run.getId());
    return new ProductionDtos.ProductionRunResponse(
        run.getId(),
        run.getProductionBatch().getId(),
        run.getRunNo(),
        run.getStepNo(),
        run.getStepName(),
        run.getStatus().name(),
        run.getRunDate(),
        run.getStartedAt(),
        run.getEndedAt(),
        run.getNotes(),
        run.getMoisturePercent(),
        inputs.stream().map(input -> new ProductionDtos.RunInputResponse(
            input.getId(),
            input.getItem().getId(),
            input.getItem().getName(),
            input.getUom().getId(),
            input.getUom().getCode(),
            input.getQuantity(),
            input.getSourceType().name(),
            input.getSourceRunOutput() != null ? input.getSourceRunOutput().getId() : null,
            input.getSourceGodown() != null ? input.getSourceGodown().getId() : null,
            input.getSourceGodown() != null ? input.getSourceGodown().getName() : null,
            input.getRate(),
            input.getAmount()
        )).toList(),
        outputs.stream().map(output -> new ProductionDtos.RunOutputResponse(
            output.getId(),
            output.getItem().getId(),
            output.getItem().getName(),
            output.getUom().getId(),
            output.getUom().getCode(),
            output.getQuantity(),
            defaultZero(output.getConsumedQuantity()),
            output.getOutputType().name(),
            output.getDestGodown() != null ? output.getDestGodown().getId() : null,
            output.getDestGodown() != null ? output.getDestGodown().getName() : null,
            output.getRate(),
            output.getAmount()
        )).toList(),
        charges.stream().map(charge -> new ProductionDtos.RunChargeResponse(
            charge.getId(),
            charge.getChargeType() != null ? charge.getChargeType().getId() : null,
            charge.getChargeType() != null ? charge.getChargeType().getName() : null,
            charge.getCalcType() != null ? charge.getCalcType().name() : null,
            charge.getRate(),
            charge.getQuantity(),
            charge.getAmount(),
            charge.isDeduction(),
            charge.getPayablePartyType() != null ? charge.getPayablePartyType().name() : null,
            charge.getPayablePartyId(),
            charge.getRemarks()
        )).toList()
    );
  }

  private ProductionDtos.WipSelectionResponse toWipSelection(ProcessRunOutput out) {
    BigDecimal available = defaultZero(out.getQuantity()).subtract(defaultZero(out.getConsumedQuantity()));
    return new ProductionDtos.WipSelectionResponse(
        out.getId(),
        out.getProcessRun().getProductionBatch().getId(),
        out.getProcessRun().getId(),
        out.getItem().getId(),
        out.getItem().getName(),
        out.getUom().getId(),
        out.getUom().getCode(),
        out.getQuantity(),
        defaultZero(out.getConsumedQuantity()),
        available
    );
  }

  private void recordMovement(String txnType, ProcessRun run, Item item, Uom uom,
                              BigDecimal qtyIn, BigDecimal qtyOut,
                              InventoryLocationType locationType, Long locationId) {
    InventoryMovement movement = new InventoryMovement();
    movement.setCompany(run.getCompany());
    movement.setTxnType(txnType);
    movement.setRefType("RUN");
    movement.setRefId(run.getId());
    movement.setItem(item);
    movement.setQtyIn(defaultZero(qtyIn));
    movement.setQtyOut(defaultZero(qtyOut));
    movement.setUom(uom);
    movement.setLocationType(locationType);
    movement.setLocationId(locationId);
    inventoryMovementRepository.save(movement);
  }

  private int nextRunNo(Long batchId) {
    Company company = requireCompany();
    return processRunRepository.findByProductionBatchIdAndCompanyId(batchId, company.getId()).stream()
        .map(ProcessRun::getRunNo)
        .filter(Objects::nonNull)
        .max(Integer::compareTo)
        .map(n -> n + 1)
        .orElse(1);
  }

  private void validateQuantity(BigDecimal qty, String label) {
    if (qty == null || qty.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException(label + " must be greater than zero");
    }
  }

  private ProductionBatch fetchBatch(Long id) {
    Company company = requireCompany();
    return productionBatchRepository.findByIdAndCompanyId(id, company.getId())
        .orElseThrow(() -> new IllegalArgumentException("Batch not found"));
  }

  private ProcessRun getRunOrThrow(Long id) {
    Company company = requireCompany();
    return processRunRepository.findByIdAndCompanyId(id, company.getId())
        .orElseThrow(() -> new IllegalArgumentException("Run not found"));
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

  private BigDecimal defaultZero(BigDecimal value) {
    return value != null ? value : BigDecimal.ZERO;
  }

  private BigDecimal resolveAmount(BigDecimal provided, BigDecimal rate, BigDecimal qty) {
    if (provided != null) {
      return provided;
    }
    if (rate == null || qty == null) {
      return BigDecimal.ZERO;
    }
    return rate.multiply(qty);
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
