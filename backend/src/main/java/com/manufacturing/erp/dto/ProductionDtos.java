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

  public record ProcessTemplateOutputRequest(
      @NotNull Long itemId,
      @NotNull Long uomId,
      @NotNull BigDecimal defaultRatio,
      @NotNull String outputType,
      String notes) {}

  public record ProcessTemplateStepChargeRequest(
      @NotNull Long chargeTypeId,
      String calcType,
      BigDecimal rate,
      Boolean perQty,
      Boolean isDeduction,
      @NotNull String payablePartyType,
      Long payablePartyId,
      String remarks) {}

  public record ProcessTemplateStepRequest(
      @NotNull Integer stepNo,
      @NotBlank String stepName,
      String stepType,
      String notes,
      List<ProcessTemplateStepChargeRequest> charges) {}

  public record ProcessTemplateRequest(
      String code,
      @NotBlank String name,
      String description,
      Long outputItemId,
      Long outputUomId,
      Boolean enabled,
      List<ProcessTemplateInputRequest> inputs,
      List<ProcessTemplateOutputRequest> outputs,
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

  public record ProcessTemplateOutputResponse(
      Long id,
      Long itemId,
      String itemName,
      Long uomId,
      String uomCode,
      BigDecimal defaultRatio,
      String outputType,
      String notes) {}

  public record ProcessTemplateStepChargeResponse(
      Long id,
      Long chargeTypeId,
      String chargeTypeName,
      String calcType,
      BigDecimal rate,
      Boolean perQty,
      Boolean isDeduction,
      String payablePartyType,
      Long payablePartyId,
      String remarks) {}

  public record ProcessTemplateStepResponse(
      Long id,
      Integer stepNo,
      String stepName,
      String stepType,
      String notes,
      List<ProcessTemplateStepChargeResponse> charges) {}

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
      List<ProcessTemplateOutputResponse> outputs,
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
      Long godownId,
      BigDecimal rate,
      BigDecimal amount) {}

  public record RunOutputRequest(
      @NotNull Long itemId,
      @NotNull Long uomId,
      @NotNull BigDecimal qty,
      @NotNull String outputType,
      Long destGodownId,
      BigDecimal rate,
      BigDecimal amount) {}

  public record RunChargeRequest(
      @NotNull Long chargeTypeId,
      String calcType,
      BigDecimal rate,
      BigDecimal quantity,
      BigDecimal amount,
      Boolean isDeduction,
      @NotNull String payablePartyType,
      Long payablePartyId,
      String remarks) {}

  public record ProductionRunRequest(
      Integer stepNo,
      String stepName,
      BigDecimal moisturePercent,
      String notes,
      LocalDate runDate,
      List<RunInputRequest> inputs,
      List<RunOutputRequest> outputs,
      List<RunChargeRequest> charges) {}

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
      String sourceGodownName,
      BigDecimal rate,
      BigDecimal amount) {}

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
      String destGodownName,
      BigDecimal rate,
      BigDecimal amount) {}

  public record RunChargeResponse(
      Long id,
      Long chargeTypeId,
      String chargeTypeName,
      String calcType,
      BigDecimal rate,
      BigDecimal quantity,
      BigDecimal amount,
      Boolean isDeduction,
      String payablePartyType,
      Long payablePartyId,
      String remarks) {}

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
      BigDecimal moisturePercent,
      List<RunInputResponse> inputs,
      List<RunOutputResponse> outputs,
      List<RunChargeResponse> charges) {}

  public record RunCostSummaryLine(
      Long outputId,
      Long itemId,
      String itemName,
      String outputType,
      BigDecimal quantity,
      BigDecimal unitCost,
      BigDecimal amount) {}

  public record RunCostSummaryResponse(
      Long runId,
      BigDecimal totalInputQty,
      BigDecimal totalInputAmount,
      BigDecimal totalOutputQty,
      BigDecimal totalOutputAmount,
      BigDecimal yieldPercent,
      BigDecimal moisturePercent,
      BigDecimal shrinkPercent,
      BigDecimal unitCost,
      List<RunCostSummaryLine> outputLines) {}

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
      String batchNo,
      Long itemId,
      String itemName,
      Long uomId,
      String uomCode,
      BigDecimal quantity,
      BigDecimal consumedQuantity,
      BigDecimal availableQuantity) {}
}
