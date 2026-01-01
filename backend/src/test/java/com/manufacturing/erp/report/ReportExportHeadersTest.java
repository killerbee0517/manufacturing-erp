package com.manufacturing.erp.report;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReportExportHeadersTest {
  @Test
  void exportedHeadersMatchContract() throws Exception {
    Path headerPath = Paths.get("..", "docs", "report-samples", "_headers.json");
    if (!Files.exists(headerPath)) {
      headerPath = Paths.get("docs", "report-samples", "_headers.json");
    }
    ObjectMapper mapper = new ObjectMapper();
    Map<String, List<String>> headerMap = mapper.readValue(
        Files.readString(headerPath),
        new TypeReference<>() {});

    for (Map.Entry<String, List<String>> entry : headerMap.entrySet()) {
      List<String> headers = entry.getValue();
      byte[] payload = ReportWorkbookBuilder.build("Contract", headers, List.of());
      try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(payload))) {
        Row row = workbook.getSheetAt(0).getRow(0);
        List<String> actual = new ArrayList<>();
        for (int i = 0; i < headers.size(); i++) {
          actual.add(row.getCell(i).getStringCellValue());
        }
        assertEquals(headers, actual, "Header mismatch for " + entry.getKey());
      }
    }
  }
}
