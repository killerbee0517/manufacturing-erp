package com.manufacturing.erp.controller;

import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.domain.Enums.QcStatus;
import com.manufacturing.erp.dto.MetricsDtos;
import com.manufacturing.erp.domain.Enums.PaymentStatus;
import com.manufacturing.erp.repository.GrnRepository;
import com.manufacturing.erp.repository.PaymentVoucherRepository;
import com.manufacturing.erp.repository.PurchaseInvoiceRepository;
import com.manufacturing.erp.repository.PurchaseOrderRepository;
import com.manufacturing.erp.repository.ProcessRunOutputRepository;
import com.manufacturing.erp.repository.QcInspectionRepository;
import com.manufacturing.erp.repository.SalesInvoiceRepository;
import com.manufacturing.erp.repository.StockLedgerRepository;
import com.manufacturing.erp.security.CompanyContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics")
public class MetricsController {
  private final GrnRepository grnRepository;
  private final QcInspectionRepository qcInspectionRepository;
  private final PurchaseInvoiceRepository purchaseInvoiceRepository;
  private final PurchaseOrderRepository purchaseOrderRepository;
  private final SalesInvoiceRepository salesInvoiceRepository;
  private final ProcessRunOutputRepository processRunOutputRepository;
  private final StockLedgerRepository stockLedgerRepository;
  private final PaymentVoucherRepository paymentVoucherRepository;
  private final CompanyContext companyContext;

  public MetricsController(GrnRepository grnRepository,
                           QcInspectionRepository qcInspectionRepository,
                           PurchaseInvoiceRepository purchaseInvoiceRepository,
                           PurchaseOrderRepository purchaseOrderRepository,
                           SalesInvoiceRepository salesInvoiceRepository,
                           ProcessRunOutputRepository processRunOutputRepository,
                           StockLedgerRepository stockLedgerRepository,
                           PaymentVoucherRepository paymentVoucherRepository,
                           CompanyContext companyContext) {
    this.grnRepository = grnRepository;
    this.qcInspectionRepository = qcInspectionRepository;
    this.purchaseInvoiceRepository = purchaseInvoiceRepository;
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.salesInvoiceRepository = salesInvoiceRepository;
    this.processRunOutputRepository = processRunOutputRepository;
    this.stockLedgerRepository = stockLedgerRepository;
    this.paymentVoucherRepository = paymentVoucherRepository;
    this.companyContext = companyContext;
  }

  @GetMapping("/dashboard")
  public MetricsDtos.DashboardMetrics dashboard() {
    Long companyId = requireCompanyId();
    long openGrns = grnRepository.countByStatusNotAndPurchaseOrderCompanyId(DocumentStatus.POSTED, companyId);
    long pendingQc = qcInspectionRepository.countByStatusAndPurchaseOrderCompanyId(QcStatus.DRAFT, companyId)
        + qcInspectionRepository.countByStatusAndPurchaseOrderCompanyId(QcStatus.SUBMITTED, companyId);
    long invoicesReady = purchaseInvoiceRepository.countByPurchaseOrderCompanyId(companyId);
    long transfersInFlight = stockLedgerRepository.countByCompanyId(companyId);
    LocalDate today = LocalDate.now();
    long pdcOverdue = paymentVoucherRepository.countByCompanyIdAndStatusAndChequeDateBefore(companyId, PaymentStatus.PDC_ISSUED, today);
    long pdcDueToday = paymentVoucherRepository.countByCompanyIdAndStatusAndChequeDate(companyId, PaymentStatus.PDC_ISSUED, today);
    long pdcFuture = paymentVoucherRepository.countByCompanyIdAndStatusAndChequeDateAfter(companyId, PaymentStatus.PDC_ISSUED, today);
    long purchaseOrdersToday = purchaseOrderRepository.countByCompanyIdAndPoDate(companyId, today);
    long poPendingApproval = purchaseOrderRepository.countByCompanyIdAndStatus(companyId, DocumentStatus.SUBMITTED);
    long salesInvoicesToday = salesInvoiceRepository.countByCompanyIdAndInvoiceDate(companyId, today);
    BigDecimal productionOutputToday = processRunOutputRepository.sumQuantityByCompanyIdAndRunDate(companyId, today);
    BigDecimal stockValue = stockLedgerRepository.sumAmountByCompanyId(companyId);
    LocalDate monthStart = today.withDayOfMonth(1);
    long purchaseOrdersMonth = purchaseOrderRepository.countByCompanyIdAndPoDateBetween(companyId, monthStart, today);
    long salesInvoicesMonth = salesInvoiceRepository.countByCompanyIdAndInvoiceDateBetween(companyId, monthStart, today);
    BigDecimal productionOutputMonth = processRunOutputRepository.sumQuantityByCompanyIdAndRunDateBetween(companyId, monthStart, today);
    return new MetricsDtos.DashboardMetrics(
        openGrns,
        pendingQc,
        invoicesReady,
        transfersInFlight,
        pdcOverdue,
        pdcDueToday,
        pdcFuture,
        purchaseOrdersToday,
        poPendingApproval,
        salesInvoicesToday,
        productionOutputToday,
        stockValue,
        purchaseOrdersMonth,
        salesInvoicesMonth,
        productionOutputMonth
    );
  }

  private Long requireCompanyId() {
    Long companyId = companyContext.getCompanyId();
    if (companyId == null) {
      throw new IllegalArgumentException("Missing company context");
    }
    return companyId;
  }
}
