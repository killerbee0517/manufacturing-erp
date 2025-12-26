package com.manufacturing.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;

public class ProductionDtos {
  public record BomLineRequest(
      @NotNull Long componentItemId,
      @NotNull Long uomId,
      @NotNull BigDecimal qtyPerUnit,
      BigDecimal scrapPercent) {}

  public record BomRequest(
      @NotNull Long finishedItemId,
      @NotBlank String name,
      String version,
      Boolean enabled,
      List<BomLineRequest> lines) {}

  public record BomLineResponse(
      Long id,
      Long componentItemId,
      String componentItemName,
      Long uomId,
      String uomCode,
      BigDecimal qtyPerUnit,
      BigDecimal scrapPercent) {}

  public record BomResponse(
      Long id,
      Long finishedItemId,
      String finishedItemName,
      String name,
      String version,
      Boolean enabled,
      List<BomLineResponse> lines) {}

  public record ProcessStepRequest(
      @NotBlank String name,
      String description,
      @NotNull Integer sequenceNo,
      Long sourceGodownId,
      Long destGodownId) {}

  public record ProcessTemplateRequest(
      @NotBlank String name,
      String description,
      List<ProcessStepRequest> steps) {}

  public record ProcessStepResponse(
      Long id,
      String name,
      String description,
      Integer sequenceNo,
      Long sourceGodownId,
      String sourceGodownName,
      Long destGodownId,
      String destGodownName) {}

  public record ProcessTemplateResponse(
      Long id,
      String name,
      String description,
      List<ProcessStepResponse> steps) {}

  public record ProductionOrderRequest(
      String orderNo,
      Long templateId,
      @NotNull Long bomId,
      @NotNull Long finishedItemId,
      @NotNull Long uomId,
      @NotNull BigDecimal plannedQty,
      LocalDate orderDate) {}

  public record ProductionOrderResponse(
      Long id,
      String orderNo,
      Long templateId,
      String templateName,
      Long bomId,
      String bomName,
      Long finishedItemId,
      String finishedItemName,
      Long uomId,
      String uomCode,
      BigDecimal plannedQty,
      LocalDate orderDate,
      String status) {}

  public record ProductionBatchResponse(
      Long id,
      String batchNo,
      Long productionOrderId,
      String status,
      LocalDate startDate,
      LocalDate endDate,
      Instant startedAt,
      Instant completedAt) {}

  public record ProcessRunInputRequest(
      @NotNull Long itemId,
      @NotNull Long uomId,
      @NotNull BigDecimal quantity,
      @NotNull String sourceType,
      Long sourceGodownId,
      Long sourceRunOutputId,
      BigDecimal rate,
      BigDecimal amount) {}

  public record ProcessRunOutputRequest(
      @NotNull Long itemId,
      @NotNull Long uomId,
      @NotNull BigDecimal quantity,
      @NotNull String outputType,
      Long destGodownId,
      BigDecimal rate,
      BigDecimal amount) {}

  public record ProcessRunRequest(
      @NotNull Long batchId,
      Long stepId,
      String stepName,
      LocalDate runDate,
      Instant startedAt,
      Instant endedAt,
      List<ProcessRunInputRequest> consumptions,
      List<ProcessRunOutputRequest> outputs) {}

  public record ProcessRunResponse(
      Long id,
      Long batchId,
      Long stepId,
      String stepName,
      LocalDate runDate,
      Instant startedAt,
      Instant endedAt,
      String status) {}

  public record BatchCostSummaryResponse(
      Long batchId,
      BigDecimal totalConsumptionQty,
      BigDecimal totalConsumptionAmount,
      BigDecimal totalOutputQty,
      BigDecimal totalOutputAmount,
      BigDecimal unitCost) {}

  public record ProcessRunInputResponse(
      Long id,
      Long itemId,
      String itemName,
      Long uomId,
      String uomCode,
      BigDecimal quantity,
      String sourceType,
      Long sourceGodownId,
      String sourceGodownName,
      Long sourceRunOutputId,
      BigDecimal rate,
      BigDecimal amount) {}

  public record ProcessRunOutputResponse(
      Long id,
      Long itemId,
      String itemName,
      Long uomId,
      String uomCode,
      BigDecimal quantity,
      String outputType,
      Long destGodownId,
      String destGodownName,
      BigDecimal rate,
      BigDecimal amount,
      BigDecimal consumedQuantity) {}

  public record WipOutputResponse(
      Long id,
      Long processRunId,
      Long itemId,
      String itemName,
      Long uomId,
      String uomCode,
      BigDecimal quantity,
      BigDecimal consumedQuantity,
      BigDecimal availableQuantity,
      BigDecimal rate) {}
}
