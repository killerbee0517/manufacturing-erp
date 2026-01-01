package com.manufacturing.erp.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.manufacturing.erp.domain.PurchaseOrder;
import com.manufacturing.erp.domain.PurchaseOrderLine;
import com.manufacturing.erp.domain.Supplier;
import com.manufacturing.erp.domain.Company;
import com.manufacturing.erp.repository.PurchaseOrderRepository;
import com.manufacturing.erp.repository.CompanyRepository;
import com.manufacturing.erp.security.CompanyContext;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PurchaseOrderPrintService {
  private final PurchaseOrderRepository purchaseOrderRepository;
  private final CompanyRepository companyRepository;
  private final CompanyContext companyContext;

  public PurchaseOrderPrintService(PurchaseOrderRepository purchaseOrderRepository,
                                   CompanyRepository companyRepository,
                                   CompanyContext companyContext) {
    this.purchaseOrderRepository = purchaseOrderRepository;
    this.companyRepository = companyRepository;
    this.companyContext = companyContext;
  }

  public byte[] renderPurchaseOrder(Long id) {
    Company company = requireCompany();
    PurchaseOrder po = purchaseOrderRepository.findByIdAndCompanyId(id, company.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Purchase order not found"));
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    Document document = new Document(PageSize.A4, 36, 36, 36, 36);
    try {
      PdfWriter.getInstance(document, outputStream);
      document.open();
      writeHeader(document, po);
      writeMetaSection(document, po);
      writeLineItems(document, po);
      writeNarrationAndTerms(document, po);
    } catch (DocumentException e) {
      throw new IllegalStateException("Failed to generate Purchase Order PDF", e);
    } finally {
      document.close();
    }
    return outputStream.toByteArray();
  }

  private void writeHeader(Document document, PurchaseOrder po) throws DocumentException {
    Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
    Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
    document.add(new Paragraph("Company Name", titleFont));
    document.add(new Paragraph("Address line 1, City, State", subtitleFont));
    document.add(new Paragraph("Phone: 000-000-0000 | Email: info@example.com", subtitleFont));
    document.add(new Paragraph(" "));
    Paragraph heading = new Paragraph("PURCHASE ORDER", titleFont);
    heading.setAlignment(Paragraph.ALIGN_CENTER);
    document.add(heading);
    document.add(new Paragraph(" "));
  }

  private void writeMetaSection(Document document, PurchaseOrder po) throws DocumentException {
    PdfPTable table = new PdfPTable(2);
    table.setWidthPercentage(100);
    Supplier supplier = po.getSupplier();
    String supplierBlock = supplier != null
        ? supplier.getName() + "\n" + defaultString(supplier.getAddress()) + "\n"
        + defaultString(supplier.getState()) + (supplier.getPinCode() != null ? " - " + supplier.getPinCode() : "") + "\n"
        + defaultString(supplier.getCountry()) + "\nGST: " + defaultString(supplier.getGstNo())
        : "-";

    table.addCell(labelCell("PO No: " + po.getPoNo()));
    table.addCell(labelCell("PO Date: " + formatDate(po.getPoDate())));
    table.addCell(labelCell("Supplier: " + (supplier != null ? supplier.getName() : "-")));
    table.addCell(labelCell("Delivery Date: " + formatDate(po.getDeliveryDate())));
    PdfPCell supplierCell = new PdfPCell(new Phrase("Supplier Details:\n" + supplierBlock));
    supplierCell.setColspan(2);
    table.addCell(supplierCell);
    document.add(table);
    document.add(new Paragraph(" "));
  }

  private void writeLineItems(Document document, PurchaseOrder po) throws DocumentException {
    PdfPTable table = new PdfPTable(new float[]{3f, 1f, 1f, 1f, 1f});
    table.setWidthPercentage(100);
    table.addCell(headerCell("Item"));
    table.addCell(headerCell("Qty"));
    table.addCell(headerCell("UOM"));
    table.addCell(headerCell("Rate"));
    table.addCell(headerCell("Amount"));

    BigDecimal total = BigDecimal.ZERO;
    for (PurchaseOrderLine line : po.getLines()) {
      BigDecimal amount = line.getAmount() != null ? line.getAmount() : line.getQuantity().multiply(line.getRate());
      total = total.add(amount);
      table.addCell(valueCell(line.getItem() != null ? line.getItem().getName() : "-"));
      table.addCell(valueCell(line.getQuantity() != null ? line.getQuantity().toPlainString() : "-"));
      table.addCell(valueCell(line.getUom() != null ? line.getUom().getCode() : "-"));
      table.addCell(valueCell(line.getRate() != null ? line.getRate().toPlainString() : "-"));
      table.addCell(valueCell(amount.toPlainString()));
    }

    PdfPCell totalLabel = new PdfPCell(new Phrase("Total"));
    totalLabel.setColspan(4);
    totalLabel.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
    table.addCell(totalLabel);
    table.addCell(valueCell(total.toPlainString()));

    document.add(table);
  }

  private void writeNarrationAndTerms(Document document, PurchaseOrder po) throws DocumentException {
    document.add(new Paragraph(" "));
    document.add(new Paragraph("Narration:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
    document.add(new Paragraph(defaultString(po.getRemarks())));
    document.add(new Paragraph(" "));
    document.add(new Paragraph("Terms & Conditions:", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
    document.add(new Paragraph("1. Goods to be delivered in good condition.\n2. Payment as per agreed terms.\n3. Taxes extra as applicable."));
  }

  private PdfPCell headerCell(String text) {
    PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
    cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
    return cell;
  }

  private PdfPCell valueCell(String text) {
    PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA, 10)));
    cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
    return cell;
  }

  private PdfPCell labelCell(String text) {
    PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA, 10)));
    cell.setBorderWidth(0);
    return cell;
  }

  private String formatDate(java.time.LocalDate date) {
    return date != null ? date.format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")) : "-";
  }

  private String defaultString(String text) {
    return text != null ? text : "";
  }

  private Company requireCompany() {
    Long companyId = companyContext.getCompanyId();
    if (companyId == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing company context");
    }
    return companyRepository.findById(companyId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found"));
  }
}
