package com.manufacturing.erp.report;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ReportWorkbookBuilder {
  private ReportWorkbookBuilder() {}

  public static byte[] build(String sheetName, List<String> headers, List<List<Object>> rows) {
    Workbook workbook = new XSSFWorkbook();
    Sheet sheet = workbook.createSheet(sheetName != null ? sheetName : "Report");
    writeHeader(sheet, headers);
    int rowIndex = 1;
    for (List<Object> rowData : rows) {
      Row row = sheet.createRow(rowIndex++);
      for (int i = 0; i < rowData.size(); i++) {
        writeCell(row, i, rowData.get(i));
      }
    }
    return toBytes(workbook);
  }

  private static void writeHeader(Sheet sheet, List<String> headers) {
    Row row = sheet.createRow(0);
    for (int i = 0; i < headers.size(); i++) {
      Cell cell = row.createCell(i);
      cell.setCellValue(headers.get(i));
    }
  }

  private static void writeCell(Row row, int index, Object value) {
    Cell cell = row.createCell(index);
    if (value == null) {
      cell.setCellValue("");
      return;
    }
    if (value instanceof Number number) {
      cell.setCellValue(number.doubleValue());
    } else {
      cell.setCellValue(String.valueOf(value));
    }
  }

  private static byte[] toBytes(Workbook workbook) {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      workbook.write(outputStream);
      workbook.close();
      return outputStream.toByteArray();
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to build report workbook", ex);
    }
  }
}
