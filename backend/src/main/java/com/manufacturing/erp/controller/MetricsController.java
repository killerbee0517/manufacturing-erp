package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.domain.Enums.QcStatus;
import com.manufacturing.erp.dto.MetricsDtos;
import com.manufacturing.erp.repository.GrnRepository;
import com.manufacturing.erp.repository.PurchaseInvoiceRepository;
import com.manufacturing.erp.repository.QcInspectionRepository;
import com.manufacturing.erp.repository.StockLedgerRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {
  private final GrnRepository grnRepository;
  private final QcInspectionRepository qcInspectionRepository;
  private final PurchaseInvoiceRepository purchaseInvoiceRepository;
  private final StockLedgerRepository stockLedgerRepository;

  public MetricsController(GrnRepository grnRepository,
                           QcInspectionRepository qcInspectionRepository,
                           PurchaseInvoiceRepository purchaseInvoiceRepository,
                           StockLedgerRepository stockLedgerRepository) {
    this.grnRepository = grnRepository;
    this.qcInspectionRepository = qcInspectionRepository;
    this.purchaseInvoiceRepository = purchaseInvoiceRepository;
    this.stockLedgerRepository = stockLedgerRepository;
  }

  @GetMapping("/dashboard")
  public MetricsDtos.DashboardMetrics dashboard() {
    long openGrns = grnRepository.countByStatusNot(DocumentStatus.POSTED);
    long pendingQc = qcInspectionRepository.countByStatus(QcStatus.PENDING);
    long invoicesReady = purchaseInvoiceRepository.count();
    long transfersInFlight = stockLedgerRepository.count();
    return new MetricsDtos.DashboardMetrics(openGrns, pendingQc, invoicesReady, transfersInFlight);
  }
}
