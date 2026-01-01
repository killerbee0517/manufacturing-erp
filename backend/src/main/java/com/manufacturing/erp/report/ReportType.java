package com.manufacturing.erp.report;

import java.util.Arrays;

public enum ReportType {
  DAILY_PRODUCTION("daily-production", "daily production chart (1).xlsx"),
  MONTHLY_PRODUCTION("monthly-production", "monthly production chart (1) (1) (1).xlsx"),
  RICE_DAILY_SUMMARY("rice-daily-summary", "Rice Daily Summary Report- 19.08.2023 (1) (1) (1).xlsx"),
  PURCHASE_STATEMENT_RICE("purchase-statement-rice", "Mothers Rice EXCEL PURCHASE STATEMENT (1) (1) (1).xlsx"),
  PURCHASE_STATEMENT_AGRO("purchase-statement-agro", "Purchase Report agro (1) (2) (1).xlsx"),
  PARTYWISE_PURCHASE_CONTRACT("partywise-purchase-contract", "Mothers Agro partywise PURCHASE contract STATEMENT (1) (1) (1).xlsx"),
  SPICES_AGRO_REPORT("spices-agro-report", "REPORT SPICES AND AGRO (1) (1) (1).xlsx"),
  FOODS_REPORT("foods-report", "Foods report as on 14-09-2023 (2) (1) (1).xlsx"),
  BANK_PAYMENT_SUMMARY("bank-payment-summary", "summarised bank payment report of mothers group (1) (1) (2).xlsx"),
  TDS_REPORT("tds-report", "TDS REPORT (1) (1) (1).xlsx");

  private final String id;
  private final String templateFileName;

  ReportType(String id, String templateFileName) {
    this.id = id;
    this.templateFileName = templateFileName;
  }

  public String getId() {
    return id;
  }

  public String getTemplateFileName() {
    return templateFileName;
  }

  public static ReportType fromId(String id) {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("Report id is required");
    }
    return Arrays.stream(values())
        .filter(type -> type.id.equalsIgnoreCase(id))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Unknown report id: " + id));
  }
}
