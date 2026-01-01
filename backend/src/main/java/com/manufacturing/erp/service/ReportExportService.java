package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Broker;
import com.manufacturing.erp.domain.Company;
import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.domain.Enums.PaymentStatus;
import com.manufacturing.erp.domain.Enums.ProcessOutputType;
import com.manufacturing.erp.domain.PaymentVoucher;
import com.manufacturing.erp.domain.ProcessRunOutput;
import com.manufacturing.erp.domain.PurchaseInvoice;
import com.manufacturing.erp.domain.PurchaseOrder;
import com.manufacturing.erp.domain.PurchaseOrderLine;
import com.manufacturing.erp.domain.Supplier;
import com.manufacturing.erp.domain.WeighbridgeTicket;
import com.manufacturing.erp.dto.ReportDtos;
import com.manufacturing.erp.report.ReportDefinitionService;
import com.manufacturing.erp.report.ReportType;
import com.manufacturing.erp.report.ReportWorkbookBuilder;
import com.manufacturing.erp.repository.BrokerRepository;
import com.manufacturing.erp.repository.CompanyRepository;
import com.manufacturing.erp.repository.ExpensePartyRepository;
import com.manufacturing.erp.repository.GrnRepository;
import com.manufacturing.erp.repository.LedgerAccountRepository;
import com.manufacturing.erp.repository.PaymentVoucherRepository;
import com.manufacturing.erp.repository.ProcessRunOutputRepository;
import com.manufacturing.erp.repository.PurchaseInvoiceRepository;
import com.manufacturing.erp.repository.PurchaseOrderRepository;
import com.manufacturing.erp.repository.SupplierRepository;
import com.manufacturing.erp.repository.WeighbridgeTicketRepository;
import com.manufacturing.erp.security.CompanyContext;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class ReportExportService {
  private final ReportDefinitionService reportDefinitionService;
  private final PaymentVoucherRepository paymentVoucherRepository;
  private final PurchaseInvoiceRepository purchaseInvoiceRepository;
  private final SupplierRepository supplierRepository;
  private final BrokerRepository brokerRepository;
  private final ExpensePartyRepository expensePartyRepository;
  private final ProcessRunOutputRepository processRunOutputRepository;
  private final PurchaseOrderRepository purchaseOrderRepository;
  private final WeighbridgeTicketRepository weighbridgeTicketRepository;
  private final GrnRepository grnRepository;
  private final LedgerAccountRepository ledgerAccountRepository;
  private final CompanyContext companyContext;
  private final CompanyRepository companyRepository;
  private final LedgerService ledgerService;

  public ReportExportService(ReportDefinitionService reportDefinitionService,
                             PaymentVoucherRepository paymentVoucherRepository,
                             PurchaseInvoiceRepository purchaseInvoiceRepository,
                             SupplierRepository supplierRepository,
                             BrokerRepository brokerRepository,
                             ExpensePartyRepository expensePartyRepository,
                             ProcessRunOutputRepository processRunOutputRepository,
                             PurchaseOrderRepository purchaseOrderRepository,
                             WeighbridgeTicketRepository weighbridgeTicketRepository,
                             GrnRepository grnRepository,
                             LedgerAccountRepository ledgerAccountRepository,
                             CompanyContext companyContext,
                             CompanyRepository companyRepository,
                             LedgerService ledgerService) {
    this.reportDefinitionService = reportDefinitionService;
    this.paymentVoucherRepository = paymentVoucherRepository;
    this.purchaseInvoiceRepository = purchaseInvoiceRepository;
    this.supplierRepository = supplierRepository;
    this.brokerRepository = brokerRepository;
    this.expensePartyRepository = expensePartyRepository;
    this.processRunOutputRepository = processRunOutputRepository;
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.weighbridgeTicketRepository = weighbridgeTicketRepository;
    this.grnRepository = grnRepository;
    this.ledgerAccountRepository = ledgerAccountRepository;
    this.companyContext = companyContext;
    this.companyRepository = companyRepository;
    this.ledgerService = ledgerService;
  }

  public ReportDtos.ReportTableResponse getReport(String reportId, ReportDtos.ReportFilter filter) {
    ReportType reportType = ReportType.fromId(reportId);
    List<String> headers = reportDefinitionService.getHeaders(reportType);
    List<List<Object>> rows = switch (reportType) {
      case DAILY_PRODUCTION -> buildDailyProductionRows(filter, headers);
      case MONTHLY_PRODUCTION -> buildMonthlyProductionRows(filter, headers);
      case RICE_DAILY_SUMMARY -> buildRiceDailySummaryRows(filter, headers);
      case PURCHASE_STATEMENT_RICE -> buildRicePurchaseStatementRows(filter, headers);
      case PURCHASE_STATEMENT_AGRO -> buildAgroPurchaseStatementRows(filter, headers);
      case PARTYWISE_PURCHASE_CONTRACT -> buildPartywisePurchaseContractRows(filter, headers);
      case SPICES_AGRO_REPORT -> buildSpicesAgroRows(headers);
      case FOODS_REPORT -> buildFoodsReportRows(headers);
      case BANK_PAYMENT_SUMMARY -> buildBankPaymentRows(filter, headers);
      case TDS_REPORT -> buildTdsRows(filter, headers);
    };
    return new ReportDtos.ReportTableResponse(reportType.getId(), headers, rows);
  }

  public byte[] exportReport(String reportId, ReportDtos.ReportFilter filter) {
    ReportType reportType = ReportType.fromId(reportId);
    List<String> headers = reportDefinitionService.getHeaders(reportType);
    List<List<Object>> rows = getReport(reportId, filter).rows();
    String sheetName = reportType.getId();
    return ReportWorkbookBuilder.build(sheetName, headers, rows);
  }

  public byte[] exportBankPaymentSummary(Long companyId, LocalDate fromDate, LocalDate toDate) {
    ReportDtos.ReportFilter filter = new ReportDtos.ReportFilter(fromDate, toDate, null, null, null, null, null);
    return exportReport(ReportType.BANK_PAYMENT_SUMMARY.getId(), filter);
  }

  public byte[] exportTdsReport(Long companyId, LocalDate fromDate, LocalDate toDate) {
    ReportDtos.ReportFilter filter = new ReportDtos.ReportFilter(fromDate, toDate, null, null, null, null, null);
    return exportReport(ReportType.TDS_REPORT.getId(), filter);
  }

  private List<List<Object>> buildDailyProductionRows(ReportDtos.ReportFilter filter, List<String> headers) {
    Company company = requireCompany();
    LocalDate fromDate = filter.fromDate();
    LocalDate toDate = filter.toDate();
    List<ProcessRunOutput> outputs = processRunOutputRepository.findByProcessRunCompanyId(company.getId());
    Map<Long, List<ProcessRunOutput>> byRun = new HashMap<>();
    for (ProcessRunOutput output : outputs) {
      if (output.getProcessRun() == null || output.getProcessRun().getRunDate() == null) {
        continue;
      }
      if (!withinRange(output.getProcessRun().getRunDate(), fromDate, toDate)) {
        continue;
      }
      byRun.computeIfAbsent(output.getProcessRun().getId(), ignored -> new ArrayList<>()).add(output);
    }
    List<List<Object>> rows = new ArrayList<>();
    for (List<ProcessRunOutput> runOutputs : byRun.values()) {
      Map<String, Object> values = new HashMap<>();
      ProcessRunOutput sample = runOutputs.get(0);
      String timeValue = sample.getProcessRun().getStartedAt() != null
          ? sample.getProcessRun().getStartedAt().toString()
          : sample.getProcessRun().getRunDate().format(DateTimeFormatter.ISO_DATE);
      values.put("TIME", timeValue);
      BigDecimal total = BigDecimal.ZERO;
      for (ProcessRunOutput output : runOutputs) {
        String itemName = output.getItem() != null ? output.getItem().getName() : null;
        if (itemName != null) {
          values.put(itemName, output.getQuantity());
        }
        total = total.add(defaultZero(output.getQuantity()));
      }
      values.put("Total With Chakki", total);
      values.put("Tot TONS W/O Chakki", total);
      values.put("CF TOTAL TONS", total);
      rows.add(buildRow(headers, values));
    }
    return rows;
  }

  private List<List<Object>> buildMonthlyProductionRows(ReportDtos.ReportFilter filter, List<String> headers) {
    Company company = requireCompany();
    LocalDate fromDate = filter.fromDate();
    LocalDate toDate = filter.toDate();
    List<ProcessRunOutput> outputs = processRunOutputRepository.findByProcessRunCompanyId(company.getId());
    Map<LocalDate, List<ProcessRunOutput>> byDate = new HashMap<>();
    for (ProcessRunOutput output : outputs) {
      if (output.getProcessRun() == null || output.getProcessRun().getRunDate() == null) {
        continue;
      }
      LocalDate runDate = output.getProcessRun().getRunDate();
      if (!withinRange(runDate, fromDate, toDate)) {
        continue;
      }
      byDate.computeIfAbsent(runDate, ignored -> new ArrayList<>()).add(output);
    }
    List<List<Object>> rows = new ArrayList<>();
    for (Map.Entry<LocalDate, List<ProcessRunOutput>> entry : byDate.entrySet()) {
      Map<String, Object> values = new HashMap<>();
      values.put("DATE", entry.getKey().format(DateTimeFormatter.ISO_DATE));
      BigDecimal total = BigDecimal.ZERO;
      for (ProcessRunOutput output : entry.getValue()) {
        String itemName = output.getItem() != null ? output.getItem().getName() : null;
        if (itemName != null) {
          values.put(itemName, output.getQuantity());
        }
        total = total.add(defaultZero(output.getQuantity()));
      }
      values.put("Total with Chakki", total);
      values.put("Total w/o Chakki WITH REF", total);
      values.put("Total w/o Chakki & WO REF", total);
      rows.add(buildRow(headers, values));
    }
    return rows;
  }

  private List<List<Object>> buildRiceDailySummaryRows(ReportDtos.ReportFilter filter, List<String> headers) {
    Company company = requireCompany();
    LocalDate fromDate = filter.fromDate();
    LocalDate toDate = filter.toDate();
    List<ProcessRunOutput> outputs = processRunOutputRepository.findByProcessRunCompanyId(company.getId()).stream()
        .filter(output -> output.getOutputType() == ProcessOutputType.FG)
        .filter(output -> output.getProcessRun() != null && withinRange(output.getProcessRun().getRunDate(), fromDate, toDate))
        .toList();
    Map<String, BigDecimal> itemTotals = new HashMap<>();
    BigDecimal total = BigDecimal.ZERO;
    for (ProcessRunOutput output : outputs) {
      String itemName = output.getItem() != null ? output.getItem().getName() : "Item";
      BigDecimal qty = defaultZero(output.getQuantity());
      itemTotals.merge(itemName, qty, BigDecimal::add);
      total = total.add(qty);
    }
    List<List<Object>> rows = new ArrayList<>();
    for (Map.Entry<String, BigDecimal> entry : itemTotals.entrySet()) {
      Map<String, Object> values = new HashMap<>();
      values.put("Item Name", entry.getKey());
      values.put("Qty(Bag)", entry.getValue());
      values.put("Particulars", "Production");
      values.put("Kg", entry.getValue());
      BigDecimal percent = total.compareTo(BigDecimal.ZERO) > 0
          ? entry.getValue().multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP)
          : BigDecimal.ZERO;
      values.put("Percentage", percent);
      rows.add(buildRow(headers, values));
    }
    return rows;
  }

  private List<List<Object>> buildRicePurchaseStatementRows(ReportDtos.ReportFilter filter, List<String> headers) {
    Company company = requireCompany();
    List<WeighbridgeTicket> tickets = weighbridgeTicketRepository.findByPurchaseOrderCompanyId(company.getId());
    List<List<Object>> rows = new ArrayList<>();
    for (WeighbridgeTicket ticket : tickets) {
      if (!withinRange(ticket.getDateIn(), filter.fromDate(), filter.toDate())) {
        continue;
      }
      if (filter.partyId() != null && (ticket.getSupplier() == null
          || !Objects.equals(ticket.getSupplier().getId(), filter.partyId()))) {
        continue;
      }
      if (filter.itemId() != null && (ticket.getItem() == null
          || !Objects.equals(ticket.getItem().getId(), filter.itemId()))) {
        continue;
      }
      Map<String, Object> values = new HashMap<>();
      values.put("Date", ticket.getDateIn());
      values.put("Item", ticket.getItem() != null ? ticket.getItem().getName() : null);
      values.put("Vehicle No.", ticket.getVehicleNo());
      values.put("Gross Wt(Kg)", ticket.getGrossWeight());
      values.put("Less", ticket.getUnloadedWeight());
      values.put("Net Wt.(Kgs)", ticket.getNetWeight());
      BigDecimal rate = resolvePoLineRate(ticket.getPurchaseOrder(), ticket.getItem());
      values.put("Rate per Kg", rate);
      if (rate != null && ticket.getNetWeight() != null) {
        values.put("Amount(Rs.)", rate.multiply(ticket.getNetWeight()));
      }
      rows.add(buildRow(headers, values));
    }
    return rows;
  }

  private List<List<Object>> buildAgroPurchaseStatementRows(ReportDtos.ReportFilter filter, List<String> headers) {
    Company company = requireCompany();
    List<PurchaseInvoice> invoices = purchaseInvoiceRepository.findByPurchaseOrderCompanyId(company.getId());
    List<List<Object>> rows = new ArrayList<>();
    for (PurchaseInvoice invoice : invoices) {
      if (!withinRange(invoice.getInvoiceDate(), filter.fromDate(), filter.toDate())) {
        continue;
      }
      if (filter.partyId() != null && (invoice.getSupplier() == null
          || !Objects.equals(invoice.getSupplier().getId(), filter.partyId()))) {
        continue;
      }
      Map<String, Object> values = new HashMap<>();
      values.put("Due date", invoice.getInvoiceDate());
      values.put("bill no/dt", invoice.getInvoiceNo());
      values.put("Party", invoice.getSupplier() != null ? invoice.getSupplier().getName() : null);
      values.put("Tot Bill Amount", invoice.getTotalAmount());
      values.put("Broker", invoice.getBroker() != null ? invoice.getBroker().getName() : null);
      values.put("Payable before tds", invoice.getTotalAmount());
      values.put("tds", invoice.getTdsAmount());
      values.put("bal payable after tds", invoice.getNetPayable());
      values.put("lab report accepted/rejected", invoice.getStatus().name());
      rows.add(buildRow(headers, values));
    }
    return rows;
  }

  private List<List<Object>> buildPartywisePurchaseContractRows(ReportDtos.ReportFilter filter, List<String> headers) {
    Company company = requireCompany();
    List<PurchaseOrder> orders = purchaseOrderRepository.findByCompanyId(company.getId());
    List<List<Object>> rows = new ArrayList<>();
    for (PurchaseOrder order : orders) {
      if (filter.partyId() != null && (order.getSupplier() == null
          || !Objects.equals(order.getSupplier().getId(), filter.partyId()))) {
        continue;
      }
      Map<String, Object> values = new HashMap<>();
      values.put("Party Name", order.getSupplier() != null ? order.getSupplier().getName() : null);
      values.put("Contract No", order.getPoNo());
      BigDecimal totalQty = order.getLines().stream()
          .map(line -> defaultZero(line.getQuantity()))
          .reduce(BigDecimal.ZERO, BigDecimal::add);
      values.put("Total Quantity", totalQty);
      values.put("Rate", order.getLines().stream().findFirst().map(PurchaseOrderLine::getRate).orElse(null));
      values.put("Tot Value", order.getTotalAmount());
      BigDecimal received = grnRepository.findByPurchaseOrderCompanyId(company.getId()).stream()
          .filter(grn -> grn.getPurchaseOrder() != null && Objects.equals(grn.getPurchaseOrder().getId(), order.getId()))
          .map(grn -> defaultZero(grn.getQuantity()))
          .reduce(BigDecimal.ZERO, BigDecimal::add);
      values.put("Received", received);
      values.put("Bal receivable", totalQty.subtract(received));
      rows.add(buildRow(headers, values));
    }
    return rows;
  }

  private List<List<Object>> buildSpicesAgroRows(List<String> headers) {
    Map<String, Object> values = new HashMap<>();
    values.put("Cash Balance", resolveLedgerBalance("Cash"));
    values.put("HDFC OD Balance", resolveLedgerBalance("HDFC OD"));
    return List.of(buildRow(headers, values));
  }

  private List<List<Object>> buildFoodsReportRows(List<String> headers) {
    Map<String, Object> values = new HashMap<>();
    values.put("Cash Balance", resolveLedgerBalance("Cash"));
    values.put("Total Fund Inflow", resolveLedgerBalance("Fund Inflow"));
    return List.of(buildRow(headers, values));
  }

  private List<List<Object>> buildBankPaymentRows(ReportDtos.ReportFilter filter, List<String> headers) {
    Company company = requireCompany();
    List<PaymentVoucher> vouchers = paymentVoucherRepository.findForExport(
        company.getId(), filter.fromDate(), filter.toDate()).stream()
        .filter(voucher -> voucher.getStatus() == PaymentStatus.POSTED
            || voucher.getStatus() == PaymentStatus.PDC_CLEARED)
        .toList();
    List<List<Object>> rows = new ArrayList<>();
    int serial = 1;
    for (PaymentVoucher voucher : vouchers) {
      if (filter.bankId() != null && (voucher.getBank() == null
          || !Objects.equals(voucher.getBank().getId(), filter.bankId()))) {
        continue;
      }
      Map<String, Object> values = new HashMap<>();
      values.put("SL. NO.", serial++);
      values.put("DATE", voucher.getVoucherDate());
      values.put("NAME", resolvePartyName(voucher));
      values.put("BANK NAME", voucher.getBank() != null ? voucher.getBank().getName() : null);
      values.put("BRANCH NAME", voucher.getBank() != null ? voucher.getBank().getBranch() : null);
      values.put("ACCOUNT NO.", voucher.getBank() != null ? voucher.getBank().getAccNo() : null);
      values.put("IFSC CODE", voucher.getBank() != null ? voucher.getBank().getIfsc() : null);
      values.put("AMOUNT", voucher.getAmount());
      values.put("ch#", voucher.getChequeNumber());
      rows.add(buildRow(headers, values));
    }
    return rows;
  }

  private List<List<Object>> buildTdsRows(ReportDtos.ReportFilter filter, List<String> headers) {
    Company company = requireCompany();
    List<PurchaseInvoice> invoices = purchaseInvoiceRepository.findByPurchaseOrderCompanyId(company.getId()).stream()
        .filter(invoice -> invoice.getStatus() == DocumentStatus.POSTED)
        .filter(invoice -> invoice.getTdsAmount() != null && invoice.getTdsAmount().compareTo(BigDecimal.ZERO) > 0)
        .filter(invoice -> withinRange(invoice.getInvoiceDate(), filter.fromDate(), filter.toDate()))
        .toList();
    List<List<Object>> rows = new ArrayList<>();
    for (PurchaseInvoice invoice : invoices) {
      Supplier supplier = invoice.getSupplier();
      Map<String, Object> values = new HashMap<>();
      values.put("Date", invoice.getInvoiceDate());
      values.put("Name and Address", supplier != null ? formatSupplierAddress(supplier) : null);
      values.put("PAN No", supplier != null ? supplier.getPan() : null);
      values.put("Bill Amount", invoice.getTotalAmount());
      values.put("(IGST/CGST+ SGST)Service Tax", invoice.getTaxTotal());
      values.put("BALANCE", invoice.getNetPayable());
      values.put("TDS deducted", invoice.getTdsAmount());
      values.put("Net Paid", invoice.getNetPayable());
      values.put("Nature of payment", "Purchase");
      values.put("Service", "Purchase");
      rows.add(buildRow(headers, values));
    }
    return rows;
  }

  private List<Object> buildRow(List<String> headers, Map<String, Object> values) {
    List<Object> row = new ArrayList<>(headers.size());
    for (String header : headers) {
      row.add(values.getOrDefault(header, null));
    }
    return row;
  }

  private String resolvePartyName(PaymentVoucher voucher) {
    if (voucher.getPartyType() == null || voucher.getPartyId() == null) {
      return null;
    }
    return switch (voucher.getPartyType()) {
      case SUPPLIER -> supplierRepository.findById(voucher.getPartyId())
          .map(Supplier::getName)
          .orElse(null);
      case BROKER -> brokerRepository.findById(voucher.getPartyId())
          .map(Broker::getName)
          .orElse(null);
      case EXPENSE -> expensePartyRepository.findById(voucher.getPartyId())
          .map(party -> party.getName())
          .orElse(null);
      case VEHICLE -> null;
    };
  }

  private String formatSupplierAddress(Supplier supplier) {
    if (supplier == null) {
      return null;
    }
    String address = supplier.getAddress();
    if (address == null || address.isBlank()) {
      return supplier.getName();
    }
    return supplier.getName() + ", " + address;
  }

  private BigDecimal resolvePoLineRate(PurchaseOrder order, com.manufacturing.erp.domain.Item item) {
    if (order == null || order.getLines() == null || item == null) {
      return null;
    }
    return order.getLines().stream()
        .filter(line -> line.getItem() != null && Objects.equals(line.getItem().getId(), item.getId()))
        .map(PurchaseOrderLine::getRate)
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }

  private BigDecimal resolveLedgerBalance(String ledgerName) {
    if (ledgerName == null) {
      return null;
    }
    Company company = requireCompany();
    return ledgerAccountRepository.findByCompanyId(company.getId()).stream()
        .filter(account -> ledgerName.equalsIgnoreCase(account.getName()))
        .findFirst()
        .map(account -> ledgerService.getBalance(account.getId()))
        .orElse(null);
  }

  private Company requireCompany() {
    Long companyId = companyContext.getCompanyId();
    if (companyId == null) {
      throw new IllegalArgumentException("Missing company context");
    }
    return companyRepository.findById(companyId)
        .orElseThrow(() -> new IllegalArgumentException("Company not found"));
  }

  private boolean withinRange(LocalDate date, LocalDate fromDate, LocalDate toDate) {
    if (date == null) {
      return false;
    }
    if (fromDate != null && date.isBefore(fromDate)) {
      return false;
    }
    return toDate == null || !date.isAfter(toDate);
  }

  private BigDecimal defaultZero(BigDecimal value) {
    return value != null ? value : BigDecimal.ZERO;
  }
}
