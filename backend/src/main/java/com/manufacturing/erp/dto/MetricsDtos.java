package com.manufacturing.erp.dto;

public class MetricsDtos {
  public record DashboardMetrics(
      long openGrns,
      long pendingQc,
      long invoicesReady,
      long transfersInFlight,
      long pdcOverdue,
      long pdcDueToday,
      long pdcFuture,
      long purchaseOrdersToday,
      long poPendingApproval,
      long salesInvoicesToday,
      java.math.BigDecimal productionOutputToday,
      java.math.BigDecimal stockValue,
      long purchaseOrdersMonth,
      long salesInvoicesMonth,
      java.math.BigDecimal productionOutputMonth) {}
}
