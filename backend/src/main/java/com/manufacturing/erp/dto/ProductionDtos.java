package com.manufacturing.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class ProductionDtos {
  public record ProcessTemplateInputRequest(
      @NotNull Long itemId,
      @NotNull Long uomId,
      @NotNull BigDecimal defaultQty,
      Boolean optional,
      String notes) {}

  public record ProcessTemplateStepRequest(
      @NotNull Integer stepNo,
      @NotBlank String stepName,
      String stepType,
      String notes) {}

  public record ProcessTemplateRequest(
      String code,
      @NotBlank String name,
      String description,
      Long outputItemId,
      Long outputUomId,
      Boolean enabled,
      List<ProcessTemplateInputRequest> inputs,
      List<ProcessTemplateStepRequest> steps) {}

  public record ProcessTemplateInputResponse(
      Long id,
      Long itemId,
      String itemName,
      Long uomId,
      String uomCode,
      BigDecimal defaultQty,
      Boolean optional,
      String notes) {}

  public record ProcessTemplateStepResponse(
      Long id,
      Integer stepNo,
      String stepName,
      String stepType,
      String notes) {}

  public record ProcessTemplateResponse(
      Long id,
      String code,
      String name,
      String description,
      Long outputItemId,
      String outputItemName,
      Long outputUomId,
      String outputUomCode,
      Boolean enabled,
      List<ProcessTemplateInputResponse> inputs,
      List<ProcessTemplateStepResponse> steps) {}

  public record ProductionOrderRequest(
      String orderNo,
      Long templateId,
      @NotNull Long finishedItemId,
      @NotNull Long uomId,
      @NotNull BigDecimal plannedQty,
      LocalDate orderDate) {}

  public record ProductionOrderResponse(
      Long id,
      String orderNo,
      Long templateId,
      String templateName,
      Long finishedItemId,
      String finishedItemName,
      Long uomId,
      String uomCode,
      BigDecimal plannedQty,
      LocalDate orderDate,
      String status) {}

  public record BatchInputResponse(
      Long id,
      Long itemId,
      String itemName,
      Long uomId,
      String uomCode,
      Integer stepNo,
      BigDecimal qty,
      String sourceType,
      Long sourceRefId,
      Long sourceGodownId,
      String sourceGodownName,
      Instant issuedAt) {}

  public record BatchOutputResponse(
      Long id,
      Long itemId,
      String itemName,
      Long uomId,
      String uomCode,
      Integer stepNo,
      BigDecimal qty,
      BigDecimal consumedQty,
      String outputType,
      Long destinationGodownId,
      String destinationGodownName,
      Instant producedAt) {}

  public record BatchStepResponse(
      Long id,
      Integer stepNo,
      String stepName,
      String status,
      Instant startedAt,
      Instant completedAt,
      String notes) {}

  public record ProductionBatchResponse(
      Long id,
      String batchNo,
      Long templateId,
      String templateName,
      Long productionOrderId,
      String status,
      BigDecimal plannedOutputQty,
      Long uomId,
      String uomCode,
      LocalDate startDate,
      LocalDate endDate,
      Instant startedAt,
      Instant completedAt,
      List<BatchInputResponse> inputs,
      List<BatchOutputResponse> outputs,
      List<BatchStepResponse> steps) {}

  public record ProductionBatchRequest(
      @NotNull Long templateId,
      BigDecimal plannedOutputQty,
      Long uomId,
      String remarks) {}

  public record BatchInputRequest(
      @NotNull Integer stepNo,
      @NotNull Long itemId,
      @NotNull Long uomId,
      @NotNull BigDecimal qty,
      @NotNull String sourceType,
      Long sourceGodownId,
      Long sourceRefId,
      Instant issuedAt) {}

  public record BatchIssueRequest(
      @NotNull List<BatchInputRequest> inputs) {}

  public record BatchStepCompleteRequest(
      String notes) {}

  public record BatchOutputRequest(
      @NotNull Integer stepNo,
      @NotNull Long itemId,
      @NotNull Long uomId,
      @NotNull BigDecimal qty,
      @NotNull String outputType,
      Long destinationGodownId,
      Instant producedAt) {}

  public record BatchProduceRequest(
      @NotNull List<BatchOutputRequest> outputs) {}

  public record BatchCostSummaryResponse(
      Long batchId,
      BigDecimal totalConsumptionQty,
      BigDecimal totalConsumptionAmount,
      BigDecimal totalOutputQty,
      BigDecimal totalOutputAmount,
      BigDecimal unitCost) {}

  public record RunInputRequest(
      @NotNull Long itemId,
      @NotNull Long uomId,
      @NotNull BigDecimal qty,
      @NotNull String sourceType,
      Long sourceRefId,
      Long godownId) {}

  public record RunOutputRequest(
      @NotNull Long itemId,
      @NotNull Long uomId,
      @NotNull BigDecimal qty,
      @NotNull String outputType,
      Long destGodownId) {}

  public record ProductionRunRequest(
      Integer stepNo,
      String stepName,
      String notes,
      LocalDate runDate,
      List<RunInputRequest> inputs,
      List<RunOutputRequest> outputs) {}

  public record RunInputResponse(
      Long id,
      Long itemId,
      String itemName,
      Long uomId,
      String uomCode,
      BigDecimal qty,
      String sourceType,
      Long sourceRefId,
      Long sourceGodownId,
      String sourceGodownName) {}

  public record RunOutputResponse(
      Long id,
      Long itemId,
      String itemName,
      Long uomId,
      String uomCode,
      BigDecimal qty,
      BigDecimal consumedQty,
      String outputType,
      Long destGodownId,
      String destGodownName) {}

  public record ProductionRunResponse(
      Long id,
      Long batchId,
      Integer runNo,
      Integer stepNo,
      String stepName,
      String status,
      LocalDate runDate,
      Instant startedAt,
      Instant endedAt,
      String notes,
      List<RunInputResponse> inputs,
      List<RunOutputResponse> outputs) {}

  public record WipSelectionResponse(
      Long id,
      Long batchId,
      Long runId,
      Long itemId,
      String itemName,
      Long uomId,
      String uomCode,
      BigDecimal quantity,
      BigDecimal consumedQuantity,
      BigDecimal availableQuantity) {}

  public record WipOutputResponse(
      Long id,
      Long batchId,
      Long itemId,
      String itemName,
      Long uomId,
      String uomCode,
      BigDecimal quantity,
      BigDecimal consumedQuantity,
      BigDecimal availableQuantity) {}
}
