package com.manufacturing.erp.dto;

import java.time.LocalDate;
import java.util.List;

public class ReportDtos {
  public record ReportFilter(
      LocalDate fromDate,
      LocalDate toDate,
      LocalDate asOnDate,
      Long partyId,
      Long itemId,
      Long godownId,
      Long bankId) {}

  public record ReportTableResponse(
      String reportId,
      List<String> headers,
      List<List<Object>> rows) {}
}
