package com.manufacturing.erp.dto;

public class MetricsDtos {
  public record DashboardMetrics(
      long openGrns,
      long pendingQc,
      long invoicesReady,
      long transfersInFlight) {}
}
