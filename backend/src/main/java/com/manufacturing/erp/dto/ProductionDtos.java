package com.manufacturing.erp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;

public class ProductionDtos {
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
      @NotBlank String orderNo,
      Long templateId,
      @NotNull Long itemId,
      @NotNull BigDecimal plannedQty,
      LocalDate orderDate) {}

  public record ProductionOrderResponse(
      Long id,
      String orderNo,
      Long templateId,
      String templateName,
      Long itemId,
      String itemName,
      BigDecimal plannedQty,
      LocalDate orderDate,
      String status) {}

  public record ProductionBatchResponse(
      Long id,
      String batchNo,
      Long productionOrderId,
      String status,
      Instant startedAt,
      Instant completedAt) {}

  public record ProcessRunItemRequest(
      @NotNull Long itemId,
      @NotNull Long uomId,
      @NotNull BigDecimal quantity,
      Long godownId) {}

  public record ProcessRunRequest(
      @NotNull Long batchId,
      @NotNull Long stepId,
      @NotNull LocalDate runDate,
      List<ProcessRunItemRequest> consumptions,
      List<ProcessRunItemRequest> outputs) {}

  public record ProcessRunResponse(
      Long id,
      Long batchId,
      Long stepId,
      String stepName,
      LocalDate runDate,
      String status) {}

  public record BatchCostSummaryResponse(
      Long batchId,
      BigDecimal totalConsumptionQty,
      BigDecimal totalOutputQty,
      BigDecimal unitCost) {}
}
