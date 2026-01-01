package com.manufacturing.erp.controller;

import com.manufacturing.erp.security.CompanyContext;
import com.manufacturing.erp.service.ReportExportService;
import java.io.IOException;
import java.time.LocalDate;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/reports")
public class ReportExportController {
  private final ReportExportService reportExportService;
  private final CompanyContext companyContext;

  public ReportExportController(ReportExportService reportExportService, CompanyContext companyContext) {
    this.reportExportService = reportExportService;
    this.companyContext = companyContext;
  }

  @GetMapping("/bank-payment-summary/export")
  public ResponseEntity<byte[]> exportBankPaymentSummary(
      @RequestParam(required = false) LocalDate fromDate,
      @RequestParam(required = false) LocalDate toDate) throws IOException {
    Long companyId = requireCompanyId();
    byte[] payload = reportExportService.exportBankPaymentSummary(companyId, fromDate, toDate);
    return buildResponse(payload, "bank-payment-summary.xlsx");
  }

  @GetMapping("/tds/export")
  public ResponseEntity<byte[]> exportTdsReport(
      @RequestParam(required = false) LocalDate fromDate,
      @RequestParam(required = false) LocalDate toDate) throws IOException {
    Long companyId = requireCompanyId();
    byte[] payload = reportExportService.exportTdsReport(companyId, fromDate, toDate);
    return buildResponse(payload, "tds-report.xlsx");
  }

  private ResponseEntity<byte[]> buildResponse(byte[] payload, String filename) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
    headers.setContentDisposition(ContentDisposition.attachment().filename(filename).build());
    return ResponseEntity.ok().headers(headers).body(payload);
  }

  private Long requireCompanyId() {
    Long companyId = companyContext.getCompanyId();
    if (companyId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing company context");
    }
    return companyId;
  }
}
