package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Enums.InventoryLocationType;
import com.manufacturing.erp.domain.Enums.LedgerTxnType;
import com.manufacturing.erp.domain.Enums.ProcessInputSourceType;
import com.manufacturing.erp.domain.Enums.ProcessOutputType;
import com.manufacturing.erp.domain.Enums.ProductionStatus;
import com.manufacturing.erp.domain.Godown;
import com.manufacturing.erp.domain.InventoryMovement;
import com.manufacturing.erp.domain.Item;
import com.manufacturing.erp.domain.ProcessRun;
import com.manufacturing.erp.domain.ProcessRunConsumption;
import com.manufacturing.erp.domain.ProcessRunOutput;
import com.manufacturing.erp.domain.ProcessTemplateStep;
import com.manufacturing.erp.domain.ProductionBatch;
import com.manufacturing.erp.domain.Uom;
import com.manufacturing.erp.dto.ProductionDtos;
import com.manufacturing.erp.repository.GodownRepository;
import com.manufacturing.erp.repository.InventoryMovementRepository;
import com.manufacturing.erp.repository.ItemRepository;
import com.manufacturing.erp.repository.ProcessRunConsumptionRepository;
import com.manufacturing.erp.repository.ProcessRunOutputRepository;
import com.manufacturing.erp.repository.ProcessRunRepository;
import com.manufacturing.erp.repository.ProcessTemplateStepRepository;
import com.manufacturing.erp.repository.ProductionBatchRepository;
import com.manufacturing.erp.repository.UomRepository;
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
  private final ProcessTemplateStepRepository processTemplateStepRepository;
  private final ItemRepository itemRepository;
  private final UomRepository uomRepository;
  private final GodownRepository godownRepository;
  private final InventoryMovementRepository inventoryMovementRepository;
  private final StockLedgerService stockLedgerService;

  public ProductionRunService(ProductionBatchRepository productionBatchRepository,
                              ProcessRunRepository processRunRepository,
                              ProcessRunConsumptionRepository processRunConsumptionRepository,
                              ProcessRunOutputRepository processRunOutputRepository,
                              ProcessTemplateStepRepository processTemplateStepRepository,
                              ItemRepository itemRepository,
                              UomRepository uomRepository,
                              GodownRepository godownRepository,
                              InventoryMovementRepository inventoryMovementRepository,
                              StockLedgerService stockLedgerService) {
    this.productionBatchRepository = productionBatchRepository;
    this.processRunRepository = processRunRepository;
    this.processRunConsumptionRepository = processRunConsumptionRepository;
    this.processRunOutputRepository = processRunOutputRepository;
    this.processTemplateStepRepository = processTemplateStepRepository;
    this.itemRepository = itemRepository;
    this.uomRepository = uomRepository;
    this.godownRepository = godownRepository;
    this.inventoryMovementRepository = inventoryMovementRepository;
    this.stockLedgerService = stockLedgerService;
  }

  @Transactional
  public ProductionDtos.ProductionRunResponse createRun(Long batchId, ProductionDtos.ProductionRunRequest request) {
    ProductionBatch batch = fetchBatch(batchId);
    ProcessRun run = new ProcessRun();
    run.setProductionBatch(batch);
    run.setRunNo(nextRunNo(batchId));
    applyStepMetadata(run, request);
    run.setRunDate(request.runDate() != null ? request.runDate() : LocalDate.now());
    run.setStatus(ProductionStatus.DRAFT);
    run.setNotes(request.notes());
    ProcessRun saved = processRunRepository.save(run);

    List<ProcessRunConsumption> inputs = buildConsumptions(saved, request.inputs());
    List<ProcessRunOutput> outputs = buildOutputs(saved, request.outputs());
    processRunConsumptionRepository.saveAll(inputs);
    processRunOutputRepository.saveAll(outputs);
    return toRunResponse(saved);
  }

  @Transactional
  public ProductionDtos.ProductionRunResponse updateRun(Long runId, ProductionDtos.ProductionRunRequest request) {
    ProcessRun run = getRunOrThrow(runId);
    applyStepMetadata(run, request);
    run.setRunDate(request.runDate() != null ? request.runDate() : run.getRunDate());
    run.setNotes(request.notes());
    processRunConsumptionRepository.deleteAll(processRunConsumptionRepository.findByProcessRunId(runId));
    processRunOutputRepository.deleteAll(processRunOutputRepository.findByProcessRunId(runId));
    processRunConsumptionRepository.saveAll(buildConsumptions(run, request.inputs()));
    processRunOutputRepository.saveAll(buildOutputs(run, request.outputs()));
    return toRunResponse(processRunRepository.save(run));
  }

  @Transactional(readOnly = true)
  public ProductionDtos.ProductionRunResponse getRun(Long id) {
    return toRunResponse(getRunOrThrow(id));
  }

  @Transactional(readOnly = true)
  public List<ProductionDtos.ProductionRunResponse> listRunsForBatch(Long batchId) {
    return processRunRepository.findByProductionBatchId(batchId).stream()
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
          || (output.getOutputType() == ProcessOutputType.BYPRODUCT && output.getDestGodown() != null);
      if (output.getOutputType() == ProcessOutputType.FG && output.getDestGodown() == null) {
        throw new IllegalArgumentException("Destination godown is required for finished outputs");
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

    run.setStatus(ProductionStatus.COMPLETED);
    run.setStartedAt(run.getStartedAt() != null ? run.getStartedAt() : Instant.now());
    run.setEndedAt(Instant.now());
    return toRunResponse(processRunRepository.save(run));
  }

  @Transactional(readOnly = true)
  public List<ProductionDtos.WipSelectionResponse> listAvailableWipForBatch(Long batchId) {
    return processRunOutputRepository.findByProcessRunProductionBatchId(batchId).stream()
        .filter(out -> out.getOutputType() == ProcessOutputType.WIP)
        .filter(out -> out.getProcessRun().getStatus() == ProductionStatus.COMPLETED)
        .map(this::toWipSelection)
        .filter(res -> res.availableQuantity().compareTo(BigDecimal.ZERO) > 0)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<ProductionDtos.WipSelectionResponse> searchWip(String search) {
    return processRunOutputRepository.findAll().stream()
        .filter(out -> out.getOutputType() == ProcessOutputType.WIP)
        .filter(out -> out.getProcessRun().getStatus() == ProductionStatus.COMPLETED)
        .filter(out -> search == null
            || out.getItem().getName().toLowerCase().contains(search.toLowerCase())
            || (out.getItem().getCode() != null
            && out.getItem().getCode().toLowerCase().contains(search.toLowerCase())))
        .map(this::toWipSelection)
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
        run.setProcessStep(matched);
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
        consumption.setSourceRunOutput(source);
      }
      consumption.setRate(BigDecimal.ZERO);
      consumption.setAmount(BigDecimal.ZERO);
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
      if ((output.getOutputType() == ProcessOutputType.FG || output.getOutputType() == ProcessOutputType.BYPRODUCT)
          && req.destGodownId() != null) {
        output.setDestGodown(fetchGodown(req.destGodownId()));
      }
      output.setConsumedQuantity(BigDecimal.ZERO);
      output.setRate(BigDecimal.ZERO);
      output.setAmount(BigDecimal.ZERO);
      return output;
    }).toList();
  }

  private ProductionDtos.ProductionRunResponse toRunResponse(ProcessRun run) {
    List<ProcessRunConsumption> inputs = processRunConsumptionRepository.findByProcessRunId(run.getId());
    List<ProcessRunOutput> outputs = processRunOutputRepository.findByProcessRunId(run.getId());
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
            input.getSourceGodown() != null ? input.getSourceGodown().getName() : null
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
            output.getDestGodown() != null ? output.getDestGodown().getName() : null
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
    return processRunRepository.findByProductionBatchId(batchId).stream()
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
    return productionBatchRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Batch not found"));
  }

  private ProcessRun getRunOrThrow(Long id) {
    return processRunRepository.findById(id)
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
}
