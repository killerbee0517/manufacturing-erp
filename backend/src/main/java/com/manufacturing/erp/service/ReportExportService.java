package com.manufacturing.erp.service;

import com.manufacturing.erp.domain.Enums.DocumentStatus;
import com.manufacturing.erp.domain.Enums.PaymentStatus;
import com.manufacturing.erp.domain.PaymentVoucher;
import com.manufacturing.erp.domain.PurchaseInvoice;
import com.manufacturing.erp.domain.Supplier;
import com.manufacturing.erp.repository.BrokerRepository;
import com.manufacturing.erp.repository.ExpensePartyRepository;
import com.manufacturing.erp.repository.PaymentVoucherRepository;
import com.manufacturing.erp.repository.PurchaseInvoiceRepository;
import com.manufacturing.erp.repository.SupplierRepository;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
public class ReportExportService {
  private static final List<String> BANK_PAYMENT_HEADERS = List.of(
      "SL. NO.",
      "DATE",
      "NAME",
      "BANK NAME",
      "BRANCH NAME",
      "ACCOUNT NO.",
      "IFSC CODE",
      "AMOUNT",
      "ch#"
  );

  private static final List<String> TDS_HEADERS = List.of(
      "Date",
      "Name and Address",
      "PAN No",
      "Bill Amount",
      "(IGST/CGST+ SGST)Service Tax",
      "BALANCE",
      "TDS deducted",
      "Net Paid",
      "Nature of payment",
      "Service"
  );

  private final PaymentVoucherRepository paymentVoucherRepository;
  private final PurchaseInvoiceRepository purchaseInvoiceRepository;
  private final SupplierRepository supplierRepository;
  private final BrokerRepository brokerRepository;
  private final ExpensePartyRepository expensePartyRepository;

  public ReportExportService(PaymentVoucherRepository paymentVoucherRepository,
                             PurchaseInvoiceRepository purchaseInvoiceRepository,
                             SupplierRepository supplierRepository,
                             BrokerRepository brokerRepository,
                             ExpensePartyRepository expensePartyRepository) {
    this.paymentVoucherRepository = paymentVoucherRepository;
    this.purchaseInvoiceRepository = purchaseInvoiceRepository;
    this.supplierRepository = supplierRepository;
    this.brokerRepository = brokerRepository;
    this.expensePartyRepository = expensePartyRepository;
  }

  public byte[] exportBankPaymentSummary(Long companyId, LocalDate fromDate, LocalDate toDate) throws IOException {
    List<PaymentVoucher> vouchers = paymentVoucherRepository.findForExport(companyId, fromDate, toDate).stream()
        .filter(voucher -> voucher.getStatus() == PaymentStatus.POSTED
            || voucher.getStatus() == PaymentStatus.PDC_CLEARED)
        .toList();
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("Bank Payment Summary");
    writeHeader(sheet, BANK_PAYMENT_HEADERS);
    int rowIndex = 1;
    int serial = 1;
    for (PaymentVoucher voucher : vouchers) {
      Row row = sheet.createRow(rowIndex++);
      writeCell(row, 0, serial++);
      writeCell(row, 1, voucher.getVoucherDate());
      writeCell(row, 2, resolvePartyName(voucher));
      writeCell(row, 3, voucher.getBank() != null ? voucher.getBank().getName() : null);
      writeCell(row, 4, voucher.getBank() != null ? voucher.getBank().getBranch() : null);
      writeCell(row, 5, voucher.getBank() != null ? voucher.getBank().getAccNo() : null);
      writeCell(row, 6, voucher.getBank() != null ? voucher.getBank().getIfsc() : null);
      writeCell(row, 7, voucher.getAmount());
      writeCell(row, 8, voucher.getChequeNumber());
    }
    return toBytes(workbook);
  }

  public byte[] exportTdsReport(Long companyId, LocalDate fromDate, LocalDate toDate) throws IOException {
    List<PurchaseInvoice> invoices = purchaseInvoiceRepository.findByPurchaseOrderCompanyId(companyId).stream()
        .filter(invoice -> invoice.getStatus() == DocumentStatus.POSTED)
        .filter(invoice -> invoice.getTdsAmount() != null && invoice.getTdsAmount().compareTo(BigDecimal.ZERO) > 0)
        .filter(invoice -> withinRange(invoice.getInvoiceDate(), fromDate, toDate))
        .toList();
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet("TDS Report");
    writeHeader(sheet, TDS_HEADERS);
    int rowIndex = 1;
    for (PurchaseInvoice invoice : invoices) {
      Supplier supplier = invoice.getSupplier();
      Row row = sheet.createRow(rowIndex++);
      writeCell(row, 0, invoice.getInvoiceDate());
      writeCell(row, 1, supplier != null ? formatSupplierAddress(supplier) : null);
      writeCell(row, 2, supplier != null ? supplier.getPan() : null);
      writeCell(row, 3, invoice.getTotalAmount());
      writeCell(row, 4, invoice.getTaxTotal());
      writeCell(row, 5, invoice.getNetPayable());
      writeCell(row, 6, invoice.getTdsAmount());
      writeCell(row, 7, invoice.getNetPayable());
      writeCell(row, 8, "Purchase");
      writeCell(row, 9, "Purchase");
    }
    return toBytes(workbook);
  }

  private void writeHeader(Sheet sheet, List<String> headers) {
    Row row = sheet.createRow(0);
    for (int i = 0; i < headers.size(); i++) {
      Cell cell = row.createCell(i);
      cell.setCellValue(headers.get(i));
    }
  }

  private void writeCell(Row row, int index, String value) {
    Cell cell = row.createCell(index);
    cell.setCellValue(value != null ? value : "");
  }

  private void writeCell(Row row, int index, LocalDate value) {
    Cell cell = row.createCell(index);
    cell.setCellValue(value != null ? value.toString() : "");
  }

  private void writeCell(Row row, int index, BigDecimal value) {
    Cell cell = row.createCell(index);
    cell.setCellValue(value != null ? value.doubleValue() : 0);
  }

  private void writeCell(Row row, int index, int value) {
    Cell cell = row.createCell(index);
    cell.setCellValue(value);
  }

  private byte[] toBytes(Workbook workbook) throws IOException {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      workbook.write(outputStream);
      workbook.close();
      return outputStream.toByteArray();
    }
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
          .map(broker -> broker.getName())
          .orElse(null);
      case EXPENSE -> expensePartyRepository.findById(voucher.getPartyId())
          .map(party -> party.getName())
          .orElse(null);
      case VEHICLE -> null;
    };
  }

  private String formatSupplierAddress(Supplier supplier) {
    String address = supplier.getAddress();
    if (address == null || address.isBlank()) {
      return supplier.getName();
    }
    return supplier.getName() + ", " + address;
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
}
